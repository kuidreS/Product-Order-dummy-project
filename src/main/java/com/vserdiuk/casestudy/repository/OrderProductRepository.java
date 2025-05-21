package com.vserdiuk.casestudy.repository;

import com.vserdiuk.casestudy.entity.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {
}
