package bo.gotthardt.ebean;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import com.google.common.base.Preconditions;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Bo Gotthardt
 */
@Slf4j
public class EbeanBundle implements ConfiguredBundle<HasDatabaseConfiguration> {
    private EbeanServer ebeanServer;

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        // Empty on purpose.
    }

    @Override
    public void run(HasDatabaseConfiguration configuration, Environment environment) throws Exception {
        ServerConfig serverConfig = getServerConfig(configuration.getDatabase());
        ebeanServer = EbeanServerFactory.create(serverConfig);
        Preconditions.checkNotNull(ebeanServer);

        environment.healthChecks().register("ebean-" + ebeanServer.getName(), new EbeanHealthCheck(ebeanServer));
    }

    public EbeanServer getEbeanServer() {
        Preconditions.checkNotNull(ebeanServer, "Ebean server not created yet (this happens during 'run' i.e. after 'initialize').");
        return ebeanServer;
    }

    private static ServerConfig getServerConfig(DataSourceFactory dbConfig) {
        ServerConfig config = new ServerConfig();
        config.setName("main");
        config.setDataSourceConfig(getDataSourceConfig(dbConfig));
        config.setDefaultServer(true);

        log.info("Connecting to database on '{}' with username '{}'.", config.getDataSourceConfig().getUrl(), config.getDataSourceConfig().getUsername());

        EbeanEntities.getEntities().forEach(config::addClass);

        // Automatically create db tables on startup. TODO remove this when using a proper database.
        config.setDdlGenerate(true);
        config.setDdlRun(true);

        return config;
    }

    private static DataSourceConfig getDataSourceConfig(DataSourceFactory dbConfig) {
        DataSourceConfig config = new DataSourceConfig();
        config.setUsername(dbConfig.getUser());
        config.setPassword(dbConfig.getPassword());
        config.setUrl(dbConfig.getUrl());
        config.setDriver(dbConfig.getDriverClass());
        config.setMinConnections(dbConfig.getMinSize());
        config.setMaxConnections(dbConfig.getMaxSize());

        return config;
    }
}
