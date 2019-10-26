package com.credibledoc.log.labelizer.datastore;

import com.credibledoc.log.labelizer.exception.LabelizerRuntimeException;
import com.mongodb.MongoClient;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.*;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.extract.ITempNaming;
import de.flapdoodle.embed.process.extract.UUIDTempNaming;
import de.flapdoodle.embed.process.io.directories.FixedPath;
import de.flapdoodle.embed.process.io.directories.IDirectory;
import de.flapdoodle.embed.process.runtime.Network;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an access to the MongoDB database.
 * 
 * @author Kyrylo Semenko
 */
public class DatastoreService {
    private static final Logger logger = LoggerFactory.getLogger(DatastoreService.class);
    private static final String DATABASE_NAME = "labelizer-db";
    private static final String DATABASE_DIR = "C:/Users/semenko/git/credibledoc/credible-doc/log-combiner-parent/log-labelizer/";
    private MongodProcess mongodProcess;
    private MongodExecutable mongodExecutable;
    private static final int DATABASE_PORT = 8083;
    private static final String LOCALHOST = "localhost";
    
    /** The connection to {@link MongoClient} through {@link Morphia} */
    private Datastore datastore;
    
    /**
     * Singleton.
     */
    private static DatastoreService instance;

    /**
     * @return The {@link DatastoreService} singleton.
     */
    public static DatastoreService getInstance() {
        if (instance == null) {
            instance = new DatastoreService();
        }
        return instance;
    }
    
    private DatastoreService() {
        startEmbeddedServer();
        logger.info("Connect to MongoDB. Host: {}, port: {}, database name: {}", LOCALHOST, DATABASE_PORT, DATABASE_NAME);
        Morphia morphia = new Morphia();
        morphia.mapPackage("com.credibledoc.log.labelizer");
        datastore = morphia.createDatastore(new MongoClient(LOCALHOST, DATABASE_PORT), DATABASE_NAME);
        datastore.ensureIndexes();
    }

    private void startEmbeddedServer() {
        try {
            int oplogSize = 50;
            String databaseDir = DATABASE_DIR + DATABASE_NAME + "/data";
            IMongodConfig mongodConfig = new MongodConfigBuilder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(DATABASE_PORT, Network.localhostIsIPv6()))
                .replication(new Storage(databaseDir, null, oplogSize))
                .build();

            IDirectory artifactStorePath = new FixedPath(DATABASE_DIR + DATABASE_NAME);
            ITempNaming executableNaming = new UUIDTempNaming();

            Command command = Command.MongoD;

            de.flapdoodle.embed.process.config.store.DownloadConfigBuilder downloadConfigBuilder =
                new DownloadConfigBuilder()
                .defaultsForCommand(command)
                .artifactStorePath(artifactStorePath);
            
            IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
                .defaults(command)
                .artifactStore(new ExtractedArtifactStoreBuilder()
                    .defaults(command)
                    .download(downloadConfigBuilder.build())
                    .executableNaming(executableNaming))
                .build();

            MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);
            mongodExecutable = runtime.prepare(mongodConfig);
            mongodProcess = mongodExecutable.start();
            if (logger.isInfoEnabled()) {
                logger.info("Database started. Executable: {}.",
                    mongodExecutable.getFile().executable().getAbsolutePath());
            }
        } catch (Exception e) {
            throw new LabelizerRuntimeException(e);
        }
        
    }

    public void stop() {
        mongodProcess.stop();
        mongodExecutable.stop();
    }

    /**
     * @return The {@link #datastore} field value.
     */
    public Datastore getDatastore() {
        return datastore;
    }
}
