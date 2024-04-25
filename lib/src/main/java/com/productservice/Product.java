
package com.productservice;

import java.math.BigDecimal;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@DynamoDbBean
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    private String productId;

	private String productName;
    
    private String description;
    
    private BigDecimal price;
    
    private String category;
    
    private String brand;
    
    private int quantityAvailable;
    
    @DynamoDbPartitionKey
    public String getProductId() {
      return productId;
    }
}
