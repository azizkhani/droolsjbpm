package org.drools.persistence.session;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;

public class DefaultJpaManager
    implements
    JpaManager {
    Environment                  env;

    private EntityManagerFactory emf;

    private EntityManager        appScopedEntityManager;
    private EntityManager        cmdScopedEntityManager;

    private boolean              internalAppScopedEntityManager;
    private boolean              internalCmdScopedEntityManager;

    public DefaultJpaManager(Environment env) {
        this.env = env;
        this.emf = ( EntityManagerFactory ) env.get( EnvironmentName.ENTITY_MANAGER_FACTORY );
    }    
    
    public EntityManager getApplicationScopedEntityManager() {
        if ( this.appScopedEntityManager == null ) {
            // Use the App scoped EntityManager if the user has provided it, and it is open.
            this.appScopedEntityManager = (EntityManager) this.env.get( EnvironmentName.APP_SCOPED_ENTITY_MANAGER );
            if ( this.appScopedEntityManager != null && !this.appScopedEntityManager.isOpen() ) {
                throw new RuntimeException("Provided APP_SCOPED_ENTITY_MANAGER is not open");
            }
            
            if ( this.appScopedEntityManager == null ) {
                internalAppScopedEntityManager = true;
                this.appScopedEntityManager = this.emf.createEntityManager();

                this.env.set( EnvironmentName.APP_SCOPED_ENTITY_MANAGER,
                              this.appScopedEntityManager );
            } else {
                internalAppScopedEntityManager = false;
            }            
        }
        return appScopedEntityManager;
    }

    public EntityManager getCommandScopedEntityManager() {
        return this.cmdScopedEntityManager;
    }    

    public void beginCommandScopedEntityManager() {
        EntityManager cmdScopedEntityManager = (EntityManager) env.get( EnvironmentName.CMD_SCOPED_ENTITY_MANAGER );
        if ( cmdScopedEntityManager == null || !cmdScopedEntityManager.isOpen() ) {
            internalCmdScopedEntityManager = true;
            cmdScopedEntityManager = this.emf.createEntityManager(); // no need to call joinTransaction as it will do so if one already exists
            this.env.set( EnvironmentName.CMD_SCOPED_ENTITY_MANAGER,
                          cmdScopedEntityManager );
        } else {
            internalCmdScopedEntityManager = false;
        }
        cmdScopedEntityManager.joinTransaction();
        appScopedEntityManager.joinTransaction();
    }

    public void endCommandScopedEntityManager() {
        if ( this.internalCmdScopedEntityManager ) {
            this.env.set( EnvironmentName.CMD_SCOPED_ENTITY_MANAGER, 
                          null );
        }
    }

    public void dispose() {
        if ( this.internalAppScopedEntityManager ) {
            if (  this.appScopedEntityManager != null && this.appScopedEntityManager.isOpen() ) {
                this.appScopedEntityManager.close();
            }
            this.internalAppScopedEntityManager = false;
            this.env.set( EnvironmentName.APP_SCOPED_ENTITY_MANAGER, null );
        }
        
        if ( this.internalCmdScopedEntityManager ) {
            if (  this.cmdScopedEntityManager != null && this.cmdScopedEntityManager.isOpen() ) {
                this.cmdScopedEntityManager.close();
            }
            this.internalCmdScopedEntityManager = false;
            this.env.set( EnvironmentName.CMD_SCOPED_ENTITY_MANAGER, null );            
        }        
    }

}
