package ru.nordmine.entities.wiki;

import javax.persistence.*;

@Entity
public class WikiRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(nullable = false)
    private WikiHeader header;

    @OneToOne
    @JoinColumn(nullable = false)
    private WikiValue value;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public WikiHeader getHeader() {
        return header;
    }

    public void setHeader(WikiHeader header) {
        this.header = header;
    }

    public WikiValue getValue() {
        return value;
    }

    public void setValue(WikiValue value) {
        this.value = value;
    }
}
