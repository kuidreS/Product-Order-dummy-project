package com.vserdiuk.casestudy.repository;
import com.vserdiuk.casestudy.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByName(String name);

}
