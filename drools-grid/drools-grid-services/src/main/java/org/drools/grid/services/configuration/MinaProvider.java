package org.drools.grid.services.configuration;

import org.drools.grid.GenericConnectorFactory;
import org.drools.grid.GenericNodeConnector;

/**
 * @author salaboy
 */
public class MinaProvider implements GenericProvider {

    private String providerAddress;
    private int providerPort;

    public MinaProvider() {
    }

    public MinaProvider(String providerAddress, int providerPort) {
        this.providerAddress = providerAddress;
        this.providerPort = providerPort;
    }

    public String getProviderAddress() {
        return providerAddress;
    }

    public int getProviderPort() {
        return providerPort;
    }

    public void setProviderAddress(String providerAddress) {
        this.providerAddress = providerAddress;
    }

    public void setProviderPort(int providerPort) {
        this.providerPort = providerPort;
    }

    public String getId() {
        return "MinaProvider:" + providerAddress + ":" + providerPort;
    }

    public ProviderType getProviderType() {
        return ProviderType.RemoteMina;
    }

    public GenericNodeConnector getConnector(String connectorString){
          return GenericConnectorFactory
                  .newConnector(connectorString+":"+this.getProviderAddress()+":"+this.getProviderPort());


    }
}
