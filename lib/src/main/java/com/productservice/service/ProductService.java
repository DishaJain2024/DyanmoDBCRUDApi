package com.productservice.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.productservice.Product;
import com.productservice.util.Utility;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.DeleteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class ProductService {
	private static String jsonBody = null;

	public APIGatewayProxyResponseEvent saveProduct(APIGatewayProxyRequestEvent apiGatewayRequest, Context context) {
		context.getLogger().log("Saving product:::" + apiGatewayRequest.getBody());
		DynamoDbTable<Product> productTableObj = initDynamoDB(context);
		context.getLogger().log("Product table created:::" + productTableObj);
		Product product = Utility.convertStringToObj(apiGatewayRequest.getBody(), context);
		productTableObj.putItem(product);
		List<Product> productList = List.of(product);
		jsonBody = Utility.convertObjToString(productList, context);
		context.getLogger().log("data saved successfully to dynamodb:::" + jsonBody);
		return createAPIResponse(jsonBody, 201, Utility.createHeaders(), context);
	}

	public APIGatewayProxyResponseEvent getProductById(APIGatewayProxyRequestEvent apiGatewayRequest, Context context) {
		String productId = apiGatewayRequest.getPathParameters().get("productId");
		context.getLogger().log("START::Request Received to product with id::" + productId);
		DynamoDbTable<Product> productTableObj = initDynamoDB(context);
		PageIterable<Product> returnedList = productTableObj
				.query(QueryConditional.keyEqualTo(Key.builder().partitionValue(productId).build()));
		List<Product> productList = returnedList.items().stream().collect(Collectors.toList());
		if (!productList.isEmpty()) {
			jsonBody = Utility.convertObjToString(productList, context);
			context.getLogger().log("END::Request Received to product with id::" + jsonBody);
			return createAPIResponse(jsonBody, 200, Utility.createHeaders(), context);
		} else {
			jsonBody = "Product Not Found Exception :" + productId;
			return createAPIResponse(jsonBody, 400, Utility.createHeaders(), context);
		}

	}

	public APIGatewayProxyResponseEvent getProducts(APIGatewayProxyRequestEvent apiGatewayRequest, Context context) {
		context.getLogger().log("START::Request Received to fetch all products");
		DynamoDbTable<Product> productTableObj = initDynamoDB(context);
		ScanEnhancedRequest scanEnhancedRequest = ScanEnhancedRequest.builder().build();
		PageIterable<Product> returnedList = productTableObj.scan();
		List<Product> productList = returnedList.items().stream().collect(Collectors.toList());
		jsonBody = Utility.convertListOfObjToString(productList, context);
		context.getLogger().log("END::Request Received to fetch all products" + jsonBody);
		return createAPIResponse(jsonBody, 200, Utility.createHeaders(), context);
	}

	public APIGatewayProxyResponseEvent deleteProductById(APIGatewayProxyRequestEvent apiGatewayRequest,
			Context context) {
		String productId = apiGatewayRequest.getPathParameters().get("productId");
		context.getLogger().log("START::Request Received to delete product with id::" + productId);
		DynamoDbTable<Product> productTableObj = initDynamoDB(context);
		Product product=
				productTableObj.deleteItem(DeleteItemEnhancedRequest.builder().key(Key.builder().partitionValue(productId).build()).build());
		List<Product> productList = List.of(product);
		if (!(Objects.isNull(product) )) {
			jsonBody = Utility.convertObjToString(productList, context);
			context.getLogger().log("END::Request Received to product with id::" + jsonBody);
			return createAPIResponse(jsonBody, 200, Utility.createHeaders(), context);
		} else {
			jsonBody = "Product Not Found Exception :" + productId;
			return createAPIResponse(jsonBody, 400, Utility.createHeaders(), context);
		}
	}

	private DynamoDbTable<Product> initDynamoDB(Context context) {
		context.getLogger().log("inside initDynamoDB");
		try {
			Region region = Region.EU_NORTH_1;
			DynamoDbClient ddb = DynamoDbClient.builder().region(region).build();

			DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder().dynamoDbClient(ddb).build();

			// Create a DynamoDbTable object
			DynamoDbTable<Product> productTable = enhancedClient.table("product", TableSchema.fromBean(Product.class));
			return productTable;
		} catch (Exception e) {
			context.getLogger().log("Exception occured inside initDynamoDB:" + e.getStackTrace());
		}
		return null;
	}

	private APIGatewayProxyResponseEvent createAPIResponse(String body, int statusCode, Map<String, String> headers,
			Context context) {
		APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
		responseEvent.setBody(body);
		responseEvent.setHeaders(headers);
		responseEvent.setStatusCode(statusCode);
		context.getLogger().log("Returning API Response");
		return responseEvent;
	}

}
