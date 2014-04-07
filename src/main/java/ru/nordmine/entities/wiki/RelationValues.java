package ru.nordmine.entities.wiki;

import javax.persistence.*;

@Entity
public class RelationValues {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(nullable = false)
    private WikiValue value;

    @OneToOne
    @JoinColumn(nullable = false)
    private WikiRelation relation;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public WikiValue getValue() {
        return value;
    }

    public void setValue(WikiValue value) {
        this.value = value;
    }

    public WikiRelation getRelation() {
        return relation;
    }

    public void setRelation(WikiRelation relation) {
        this.relation = relation;
    }
}
