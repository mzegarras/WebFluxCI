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

When('the client create product {string} and price {float}') do |string, float|
		
	@response_post = @req.post("productos", 
			{"nombre": string,"precio": float,"createAt": "2020-10-01T11:57:17.837+00:00","categoria": {"id": "5f75c41d4fccd7044d16bc51","nombre": "TVs"}}.to_json)

	print @response_post
end

Then('the product\'s response should be JSON:') do |doc_string|
	
end	