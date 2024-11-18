package io.github.zorin95670.specification;

import io.github.zorin95670.predicate.FilterType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Entity
@Data
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
}
