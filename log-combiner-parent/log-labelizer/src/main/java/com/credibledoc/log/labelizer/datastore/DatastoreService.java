package com.credibledoc.log.labelizer.datastore;

import com.credibledoc.log.labelizer.config.Config;
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

import java.io.IOException;
import java.net.Socket;

/**
 * Provides an access to the MongoDB database.
 * 
 * @author Kyrylo Semenko
 */
public class DatastoreService {
    private static final Logger logger = LoggerFactory.getLogger(DatastoreService.class);
    private static final String DATABASE_NAME = "labelizer-db";
    private MongodProcess mongodProcess;
    private MongodExecutable mongodExecutable;
    private static final int DATABASE_PORT = 8083;
    private static final String LOCALHOST = "localhost";
    
    /**
     * The connection to {@link MongoClient} through {@link Morphia}
     */
    private Datastore datastore;

    /**
     * Should be the DB server stopped at the end of the work?
     */
    private boolean shouldBeStopped = false;
    
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
        if (!available()) {
            startEmbeddedServer();
            shouldBeStopped = true;
        }
        logger.info("Connect to MongoDB. Host: {}, port: {}, database name: {}", LOCALHOST, DATABASE_PORT, DATABASE_NAME);
        Morphia morphia = new Morphia();
        morphia.mapPackage("com.credibledoc.log.labelizer");
        MongoClient mongoClient = new MongoClient(LOCALHOST, DATABASE_PORT);
        datastore = morphia.createDatastore(mongoClient, DATABASE_NAME);
        datastore.ensureIndexes();
    }

    private static boolean available() {
        try (Socket socket = new Socket(LOCALHOST, DATABASE_PORT)) {
            logger.info("Database server already running. Socket: {}", socket);
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    private void startEmbeddedServer() {
        try {
            int oplogSize = 50;
            String configDatabaseDir = Config.getDatabaseDir();
            String databaseDir = configDatabaseDir + DATABASE_NAME + "/data";
            IMongodConfig mongodConfig = new MongodConfigBuilder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(DATABASE_PORT, Network.localhostIsIPv6()))
                .replication(new Storage(databaseDir, null, oplogSize))
                .build();

            IDirectory artifactStorePath = new FixedPath(configDatabaseDir + DATABASE_NAME);
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
        if (shouldBeStopped) {
            mongodProcess.stop();
            mongodExecutable.stop();
        }
    }

    /**
     * @return The {@link #datastore} field value.
     */
    public Datastore getDatastore() {
        return datastore;
    }
}
