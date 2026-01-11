package com.theanh.lms.entity;

import com.theanh.lms.common.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart_item")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CartItem extends BaseAuditEntity {

    @Column(name = "cart_id", nullable = false)
    private Long cartId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "price_cents", nullable = false)
    private Long priceCents;

    @Column(name = "final_price_cents", nullable = false)
    private Long finalPriceCents;
}
