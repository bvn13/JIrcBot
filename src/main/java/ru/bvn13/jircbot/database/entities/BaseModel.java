package ru.bvn13.jircbot.database.entities;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by bvn13 on 31.01.2018.
 */
@MappedSuperclass
public abstract class BaseModel implements Comparable<BaseModel>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Getter
    @Setter
    private Long id;

    @Column(nullable = false)
    @Getter
    @Setter
    private Date dtCreated;

    @Column(nullable = false)
    @Getter
    @Setter
    private Date dtUpdated;

    @PrePersist
    public void prePersist(){
        dtCreated = dtUpdated = new Date();
    }

    @PreUpdate
    public void preUpdate(){
        dtUpdated = new Date();
    }

    @Override
    public int compareTo(BaseModel o) {
        return this.getId().compareTo(o.getId());
    }

    public int hashCode() {
        return new HashCodeBuilder().append(getId()).toHashCode();
    }


//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long _id) {
//        id = _id;
//    }
//
//    public Date getDtCreated() {
//        return dtCreated;
//    }
//
//    public void setDtCreated(Date dtCreated) {
//        this.dtCreated = dtCreated;
//    }
//
//    public Date getDtUpdated() {
//        return dtUpdated;
//    }
//
//    public void setDtUpdated(Date dtUpdated) {
//        this.dtUpdated = dtUpdated;
//    }

}
