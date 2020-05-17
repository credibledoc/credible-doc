package com.credibledoc.substitution.reporting.tracking;

import com.credibledoc.substitution.core.context.SubstitutionContext;
import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;
import com.credibledoc.substitution.core.resource.ResourceService;
import com.credibledoc.substitution.core.resource.TemplateResource;
import com.credibledoc.substitution.core.pair.Pair;
import com.credibledoc.substitution.reporting.replacement.ReplacementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

/**
 * Watches directories for changes to files and runs template generation in case when some template in the
 * tracked directory changed.
 * <p>
 * This stateful object contains {@link #watchService}, {@link #map}, {@link #substitutionContext}
 * and {@link #fragmentDependencyMap} instances.
 *
 * @author Kyrylo Semenko
 */
public class TrackingService {
    private static final Logger logger = LoggerFactory.getLogger(TrackingService.class);

    /**
     * See the {@link WatchService} description.
     */
    private final WatchService watchService;

    /**
     * Contains {@link Path}s watched by {@link WatchKey}s.
     */
    private final Map<WatchKey, Path> map;

    /**
     * Contains the application current state.
     */
    private final SubstitutionContext substitutionContext;

    /**
     * Key is a fragment that can be changed, and Values are dependant files which should be
     * generated in case of the fragment change.
     */
    private final Map<Path, Set<Path>> fragmentDependencyMap;

    public TrackingService(SubstitutionContext substitutionContext) throws IOException {
        this.watchService = FileSystems.getDefault().newWatchService();
        this.map = new HashMap<>();
        this.substitutionContext = substitutionContext;
        this.fragmentDependencyMap = new HashMap<>();
    }

    private void addFromRepository() {
        try {
            Set<Path> toRegister = new HashSet<>();
            for (Pair<Path, Path> pair : substitutionContext.getTrackableRepository().getPairs()) {
                Path fragmentPath = pair.getLeft();
                Path templatePath = pair.getRight();
                if (fragmentDependencyMap.containsKey(fragmentPath)) {
                    fragmentDependencyMap.get(fragmentPath).add(templatePath);
                } else {
                    HashSet<Path> value = new HashSet<>();
                    value.add(templatePath);
                    fragmentDependencyMap.put(fragmentPath, value);
                }
                toRegister.add(fragmentPath.getParent());
                Path parent = fragmentPath.getParent().getParent();
                if (parent != null) {
                    toRegister.add(parent);
                }
            }
            for (Path path : toRegister) {
                register(path);
            }
        } catch (Exception e) {
            throw new SubstitutionRuntimeException(e);
        }
    }

    public void track() throws IOException, InterruptedException {
        addFromRepository();
        ResourceService resourceService = ResourceService.getInstance();
        String templatesResource = substitutionContext.getConfiguration().getTemplatesResource();
        File templatesDir = resourceService.findTemplatesDir(templatesResource);
        Path path = templatesDir.toPath();
        registerAll(path);
        processEvents();
    }

    private void processEvents() throws IOException, InterruptedException {
        while (true) {
            // wait for key to be signalled
            WatchKey key = watchService.take();

            TrackingResult trackingResult = processWatchKey(key);
            if (TrackingResult.FAILED == trackingResult) {
                break;
            }
        }
    }

    private TrackingResult processWatchKey(WatchKey watchKey) throws IOException {
        Path dir = map.get(watchKey);
        if (dir == null) {
            logger.error("WatchKey not recognized.");
            return TrackingResult.FAILED;
        }

        for (WatchEvent<?> event : watchKey.pollEvents()) {
            processEvent(watchKey, dir, event);
        }

        // reset key and remove from set if directory no longer accessible
        boolean valid = watchKey.reset();
        if (!valid) {
            logger.trace("WatchKey will be deleted '{}'", dir);
            map.remove(watchKey);

            // all directories are inaccessible
            if (map.isEmpty()) {
                return TrackingResult.SUCCESSFUL;
            }
        }
        return TrackingResult.SUCCESSFUL;
    }

    @SuppressWarnings("unchecked")
    private void processEvent(WatchKey watchKey, Path dir, WatchEvent<?> event) throws IOException {
        WatchEvent.Kind<?> kind = event.kind();

        if (kind == OVERFLOW) {
            logger.error("WatchKey OVERFLOW");
            return;
        }

        // Context for the directory entry event is a file name of entry
        WatchEvent<Path> ev = (WatchEvent<Path>) event;
        Path name = ev.context();
        Path path = dir.resolve(name);

        // if fragment is deleted
        boolean fragmentDeleted = kind == ENTRY_DELETE && isFragment(path);
        if (!path.toString().endsWith("~") && fragmentDeleted) {
            deleteFragment(path);
            return;
        }

        // if a directory is deleted
        if (kind == ENTRY_DELETE && (Files.isDirectory(path) || map.containsValue(path))) {
            deleteDir(path);
        }

        // if a directory is created, then register it and all its sub-directories
        if (kind == ENTRY_CREATE && Files.isDirectory(path)) {
            createDir(path);
        }

        // if a file is created or changed, then find placeholder(s) and if some exist, generate content from the template
        if (!path.toString().endsWith("~") && Files.isRegularFile(path) &&
                watchKey.pollEvents().isEmpty()) {
            processFile(kind, path);
        }
    }

    private boolean isFragment(Path path) {
        String templatesResource = substitutionContext.getConfiguration().getTemplatesResource();
        Path templatesPath = ResourceService.getInstance().findTemplatesDir(templatesResource).toPath();
        String templatesPathNormalized = templatesPath.toAbsolutePath().normalize().toString();
        String pathNormalized = path.toAbsolutePath().normalize().toString();
        return !pathNormalized.startsWith(templatesPathNormalized);
    }

    private void processFile(WatchEvent.Kind<?> kind, Path path) {
        try {
            ReplacementService replacementService = ReplacementService.getInstance();
            boolean isFragment = isFragment(path);
            if (isFragment && fragmentDependencyMap.containsKey(path)) {
                for (Path templatePath : fragmentDependencyMap.get(path)) {
                    TemplateResource templateResource = new TemplateResource(templatePath);
                    replacementService.insertContentIntoTemplate(templateResource, substitutionContext);
                }
            }
            if (!isFragment) {
                TemplateResource templateResource = new TemplateResource(path);
                if (kind == ENTRY_DELETE) {
                    File generatedFile =
                        replacementService.getTargetFile(templateResource, substitutionContext);
                    logger.trace("File will be deleted '{}'", generatedFile.getAbsolutePath());
                    Files.deleteIfExists(generatedFile.toPath());
                } else {
                    replacementService.insertContentIntoTemplate(templateResource, substitutionContext);
                }
            }
        } catch (Exception e) {
            logger.trace(e.getMessage(), e);
        }
    }

    private void deleteFragment(Path path) {
        try {
            ReplacementService replacementService = ReplacementService.getInstance();
            boolean isFragment = isFragment(path);
            if (isFragment) {
                for (Path nextKey : getChildrenFragmentTemplates(path)) {
                    TemplateResource templateResource = new TemplateResource(nextKey);
                    replacementService.insertContentIntoTemplate(templateResource, substitutionContext);
                }
            }
        } catch (Exception e) {
            logger.trace(e.getMessage(), e);
        }
    }

    private Set<Path> getChildrenFragmentTemplates(Path path) {
        Set<Path> result = new HashSet<>();
        String dirName = path.normalize().toString();
        for (Map.Entry<Path, Set<Path>> entry : fragmentDependencyMap.entrySet()) {
            Path mapKey = entry.getKey();
            String mapKeyName = mapKey.toString();
            if (mapKeyName.startsWith(dirName)) {
                result.addAll(entry.getValue());
            }
        }
        return result;
    }

    private void createDir(Path path) throws IOException {
        if (!isFragment(path)) {
            ReplacementService replacementService = ReplacementService.getInstance();
            TemplateResource templateResource = new TemplateResource(path);
            File generatedDir = replacementService.getTargetFile(templateResource, substitutionContext);
            Files.createDirectories(generatedDir.toPath());
        }
        registerAll(path);
    }

    private void deleteDir(Path path) throws IOException {
        ReplacementService replacementService = ReplacementService.getInstance();
        TemplateResource templateResource = new TemplateResource(path);
        File generatedDir = replacementService.getTargetFile(templateResource, substitutionContext);
        deleteDirRecursively(generatedDir);
    }

    private void deleteDirRecursively(File directoryToDelete) throws IOException {
        File[] allContents = directoryToDelete.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirRecursively(file);
            }
        }
        logger.trace("File will be deleted '{}'", directoryToDelete.getAbsolutePath());
        Files.deleteIfExists(directoryToDelete.toPath());
    }

    /**
     * Register the given directory, all its sub-directories and all files, with the {@link #watchService}.
     * 
     * @param start the root path for watching
     * @throws IOException in case of read or write exceptions
     */
    private void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) throws IOException {
                register(path);
                reloadFragments(path);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void reloadFragments(Path path) {
        ReplacementService replacementService = ReplacementService.getInstance();
        if (isFragment(path)) {
            File[] files = path.toFile().listFiles();
            if (files == null) {
                return;
            }
            for (File file : files) {
                if (file.isFile() && isFragment(file.toPath()) && fragmentDependencyMap.containsKey(file.toPath())) {
                    for (Path templatePath : fragmentDependencyMap.get(file.toPath())) {
                        TemplateResource templateResource = new TemplateResource(templatePath);
                        replacementService.insertContentIntoTemplate(templateResource, substitutionContext);
                    }
                }
            }
        }
    }

    /**
     * Register the given directory with the WatchService
     * @param path the directory for watching
     * @throws IOException in case of read or write exceptions
     */
    private void register(Path path) throws IOException {
        WatchKey key = path.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        map.put(key, path);
    }

}
