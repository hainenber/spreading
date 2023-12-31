package com.hainenber.spreading.hackernews;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

import java.util.List;

@Getter
@Entity
public class Item {
    @Id
    private String id;
    private Boolean deleted;
    private String type;
    private Long time;
    private String text;
    private Boolean dead;
    private Long parent;
    private Long poll;
    private List<Long> kids;
    private String url;
    private Integer score;
    private String title;
    private List<Long> parts;
    private Integer descendants;

    // Default constructor used by JPA, not for human usage.
    protected Item() {}

    public Item(
            String id,
            Boolean deleted,
            String type,
            Long time,
            String text,
            Boolean dead,
            Long parent,
            Long poll,
            List<Long> kids,
            String url,
            Integer score,
            String title,
            List<Long> parts,
            Integer descendants
    ) {
        this.id = id;
        this.deleted = deleted;
        this.type = type;
        this.time = time;
        this.text = text;
        this.dead = dead;
        this.parent = parent;
        this.poll = poll;
        this.kids = kids;
        this.url = url;
        this.score = score;
        this.title = title;
        this.parts = parts;
        this.descendants = descendants;
    }
}
