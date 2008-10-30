package org.drools.lang.descr;

import org.drools.rule.Dialectable;
import org.drools.rule.Namespaceable;

public class ProcessDescr extends BaseDescr
    implements
    Dialectable,
    Namespaceable {

    private String namespace;
    private String dialect;
    private String name;
    private String className;
    private String url;

    public ProcessDescr() {

    }

    public ProcessDescr(String name) {
        super();
        this.name = name;
    }    
    
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public void setDialect(String dialect) {
        this.dialect = dialect;
    }

    public String getDialect() {
        return this.dialect;
    }

}
