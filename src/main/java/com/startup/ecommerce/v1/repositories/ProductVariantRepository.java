package com.startup.ecommerce.v1.repositories;

import com.startup.ecommerce.v1.entities.ProductVariantEntity;
import com.startup.ecommerce.v1.entities.ProductEntity;
import com.startup.ecommerce.v1.entities.enums.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariantEntity, Long> {
    boolean existsByProductAndSizeAndColorHexIgnoreCase(ProductEntity product, Size size, String colorHex);
}
