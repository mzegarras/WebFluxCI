# encoding: utf-8

Before do
	url = 'http://microservice:8080/'
	
	@schema = {
		"type" => "object"
	}
	
	@req = Http_Request.new url, {}
end

After do
end

When('the client requests GET \/fruits') do
	@response = @req.get("productos")
	print @response
end

Then('the response should be JSON:') do |doc_string|
	#pending # Write code here that turns the phrase above into concrete actions
end

Given('the system knows about the following fruit:') do |table|
	#pending
end	


# // Business logic execution
# Flux<Producto> productoFlux = client.get().uri("/productos")
# 		.exchange()
# 		.expectStatus().isOk()
# 		.expectHeader().contentType(MediaType.APPLICATION_JSON)
# 		.returnResult(Producto.class)
# 		.getResponseBody();