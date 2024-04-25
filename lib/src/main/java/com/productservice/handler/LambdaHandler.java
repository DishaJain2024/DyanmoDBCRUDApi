package com.productservice.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.productservice.service.ProductService;

public class LambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent apiGatewayRequest, Context context) {
		context.getLogger().log("Request received by handleRequest");
		ProductService productService = new ProductService();
		switch (apiGatewayRequest.getHttpMethod()) {

		case "POST":
			return productService.saveProduct(apiGatewayRequest, context);

		case "GET":
			if (apiGatewayRequest.getPathParameters() != null) {
				return productService.getProductById(apiGatewayRequest, context);
			}
			return productService.getProducts(apiGatewayRequest, context);

		case "DELETE":
			if (apiGatewayRequest.getPathParameters() != null) {
				return productService.deleteProductById(apiGatewayRequest, context);
			}

		default:
			throw new Error("Unsupported Methods:::" + apiGatewayRequest.getHttpMethod());

		}
	}
}
