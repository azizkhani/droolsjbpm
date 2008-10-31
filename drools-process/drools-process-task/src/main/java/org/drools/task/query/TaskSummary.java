package org.drools.task.query;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;

import org.drools.task.Status;
import org.drools.task.User;

public class TaskSummary
    implements
    Externalizable {
    private long    id;

    private String  name;

    private String  subject;

    private String  description;

    private Status  status;

    private int     priority;

    private boolean skipable;

    private User    actualOwner;

    private User    createdBy;

    private Date    createdOn;

    private Date    activationTime;

    private Date    expirationTime;

    public TaskSummary(long id,
                       String name,
                       String subject,
                       String description,
                       Status status,
                       int priority,
                       boolean skipable,
                       User actualOwner,
                       User createdBy,
                       Date createdOn,
                       Date activationTime,
                       Date expirationTime) {
        super();
        this.id = id;
        this.name = name;
        this.subject = subject;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.skipable = skipable;
        this.actualOwner = actualOwner;
        this.createdBy = createdBy;
        this.createdOn = createdOn;
        this.activationTime = activationTime;
        this.expirationTime = expirationTime;
    }

    public TaskSummary() {
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong( id );

        if ( name != null ) {
            out.writeBoolean( true );
            out.writeUTF( name );
        } else {
            out.writeBoolean( false );
        }
        if ( subject != null ) {
            out.writeBoolean( true );
            out.writeUTF( subject );
        } else {
            out.writeBoolean( false );
        }

        if ( description != null ) {
            out.writeBoolean( true );
            out.writeUTF( description );
        } else {
            out.writeBoolean( false );
        }

        if ( status != null ) {
            out.writeBoolean( true );
            out.writeUTF( status.toString() );
        } else {
            out.writeBoolean( false );
        }

        out.writeInt( priority );
        out.writeBoolean( skipable );

        if ( actualOwner != null ) {
            out.writeBoolean( true );
            actualOwner.writeExternal( out );
        } else {
            out.writeBoolean( false );
        }

        if ( createdBy != null ) {
            out.writeBoolean( true );
            createdBy.writeExternal( out );
        } else {
            out.writeBoolean( false );
        }
        if ( createdOn != null ) {
            out.writeBoolean( true );
            out.writeLong( createdOn.getTime() );
        } else {
            out.writeBoolean( false );
        }

        if ( activationTime != null ) {
            out.writeBoolean( true );
            out.writeLong( activationTime.getTime() );
        } else {
            out.writeBoolean( false );
        }

        if ( expirationTime != null ) {
            out.writeBoolean( true );
            out.writeLong( expirationTime.getTime() );
        } else {
            out.writeBoolean( false );
        }
    }

    public void readExternal(ObjectInput in) throws IOException,
                                            ClassNotFoundException {
        id = in.readLong();

        if ( in.readBoolean() ) {
            name = in.readUTF();
        }

        if ( in.readBoolean() ) {
            subject = in.readUTF();
        }
        
        if ( in.readBoolean() ) {
            description = in.readUTF();
        }
        
        if ( in.readBoolean() ) {
            status = Status.valueOf( in.readUTF() );
        }
        
        priority = in.readInt();
        skipable = in.readBoolean();

        if ( in.readBoolean() ) {
            actualOwner = new User();
            actualOwner.readExternal( in );
        }
        
        if ( in.readBoolean() ) {
            createdBy = new User();
            createdBy.readExternal( in );
        }
        
        if ( in.readBoolean() ) {
            createdOn = new Date( in.readLong() );
        }
        
        if ( in.readBoolean() ) {
            activationTime = new Date( in.readLong() );
        }
        
        if ( in.readBoolean() ) {
            expirationTime = new Date( in.readLong() );
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isSkipable() {
        return skipable;
    }

    public void setSkipable(boolean skipable) {
        this.skipable = skipable;
    }

    public User getActualOwner() {
        return actualOwner;
    }

    public void setActualOwner(User actualOwner) {
        this.actualOwner = actualOwner;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Date getActivationTime() {
        return activationTime;
    }

    public void setActivationTime(Date activationTime) {
        this.activationTime = activationTime;
    }

    public Date getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Date expirationTime) {
        this.expirationTime = expirationTime;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((activationTime == null) ? 0 : activationTime.hashCode());
        result = prime * result + ((actualOwner == null) ? 0 : actualOwner.hashCode());
        result = prime * result + ((createdBy == null) ? 0 : createdBy.hashCode());
        result = prime * result + ((createdOn == null) ? 0 : createdOn.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((expirationTime == null) ? 0 : expirationTime.hashCode());
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + priority;
        result = prime * result + (skipable ? 1231 : 1237);
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((subject == null) ? 0 : subject.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( !(obj instanceof TaskSummary) ) return false;
        TaskSummary other = (TaskSummary) obj;
        if ( activationTime == null ) {
            if ( other.activationTime != null ) return false;
        } else if ( activationTime.getTime() != other.activationTime.getTime() ) return false;
        if ( actualOwner == null ) {
            if ( other.actualOwner != null ) return false;
        } else if ( !actualOwner.equals( other.actualOwner ) ) return false;
        if ( createdBy == null ) {
            if ( other.createdBy != null ) return false;
        } else if ( !createdBy.equals( other.createdBy ) ) return false;
        if ( createdOn == null ) {
            if ( other.createdOn != null ) return false;
        } else if ( createdOn.getTime() != other.createdOn.getTime() ) return false;
        if ( description == null ) {
            if ( other.description != null ) return false;
        } else if ( !description.equals( other.description ) ) return false;
        if ( expirationTime == null ) {
            if ( other.expirationTime != null ) return false;
        } else if ( expirationTime.getTime() != other.expirationTime.getTime() ) return false;
        if ( name == null ) {
            if ( other.name != null ) return false;
        } else if ( !name.equals( other.name ) ) return false;
        if ( priority != other.priority ) return false;
        if ( skipable != other.skipable ) return false;
        if ( status == null ) {
            if ( other.status != null ) return false;
        } else if ( !status.equals( other.status ) ) return false;
        if ( subject == null ) {
            if ( other.subject != null ) return false;
        } else if ( !subject.equals( other.subject ) ) return false;
        return true;
    }

}
