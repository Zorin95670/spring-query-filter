package io.github.zorin95670.specification;

import io.github.zorin95670.predicate.FilterType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Entity
public class MyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @FilterType(type=Long.class)
    private Long id;

    @Column
    @FilterType(type=String.class)
    private String text;

    @Column
    @FilterType(type= Date.class)
    private Date date;

    @Column
    @FilterType(type= UUID.class)
    private UUID uuid;

    @Column
    @FilterType(type=Integer.class)
    private int numberInteger;

    @Column
    @FilterType(type=Float.class)
    private float numberFloat;

    @Column
    @FilterType(type=Double.class)
    private double numberDouble;

    @Column
    private String unfilteredField;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public int getNumberInteger() {
        return numberInteger;
    }

    public void setNumberInteger(int numberInteger) {
        this.numberInteger = numberInteger;
    }

    public float getNumberFloat() {
        return numberFloat;
    }

    public void setNumberFloat(float numberFloat) {
        this.numberFloat = numberFloat;
    }

    public double getNumberDouble() {
        return numberDouble;
    }

    public void setNumberDouble(double numberDouble) {
        this.numberDouble = numberDouble;
    }

    public String getUnfilteredField() {
        return unfilteredField;
    }

    public void setUnfilteredField(String unfilteredField) {
        this.unfilteredField = unfilteredField;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyEntity myEntity = (MyEntity) o;
        return Objects.equals(id, myEntity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
