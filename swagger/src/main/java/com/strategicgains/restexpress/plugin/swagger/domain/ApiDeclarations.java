/*
    Copyright 2013, Strategic Gains, Inc.

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package com.strategicgains.restexpress.plugin.swagger.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.restexpress.RestExpress;
import org.restexpress.route.Route;

import com.strategicgains.restexpress.plugin.swagger.ModelResolver;
import com.strategicgains.restexpress.plugin.swagger.annotations.ApiModelRequest;

/**
 * @author toddf
 * @since Nov 21, 2013
 * @see https://github.com/wordnik/swagger-core/wiki/Api-Declaration
 */
public class ApiDeclarations
{
	private String apiVersion;
	private String swaggerVersion;
	private String basePath;
	private String resourcePath;
	private List<String> consumes;
	private List<String> produces;
	private List<ApiDeclaration> apis = new ArrayList<ApiDeclaration>();
	private transient Map<String, ApiDeclaration> apisByPath = new LinkedHashMap<String, ApiDeclaration>();
	private Map<String, ApiModel> models = new HashMap<String, ApiModel>();

	public ApiDeclarations(ApiResources api, RestExpress server, String path)
	{
		this.apiVersion = api.getApiVersion();
		this.swaggerVersion = api.getSwaggerVersion();
		this.basePath = server.getBaseUrl();
		this.resourcePath = path;
	}

	public void addApi(ApiDeclaration api)
	{
		apis.add(api);
		apisByPath.put(api.getPath(), api);
	}

	public ApiDeclarations consumes(String contentType)
	{
		if (consumes == null)
		{
			consumes = new ArrayList<String>();
		}

		if (!consumes.contains(contentType))
		{
			consumes.add(contentType);
		}

		return this;
	}

	public ApiDeclarations produces(String contentType)
	{
		if (produces == null)
		{
			produces = new ArrayList<String>();
		}

		if (!produces.contains(contentType))
		{
			produces.add(contentType);
		}

		return this;
	}

	public ApiDeclarations addModel(ApiModel model)
	{
		if (!models.containsKey(model.getId()))
		{
			models.put(model.getId(), model);
		}

		return this;
	}

	public Map<String, ApiModel> getModels()
	{
		return models;
	}

	public ApiOperation addOperation(Route route)
	{
		ApiDeclaration apiDeclaration = apisByPath.get(route.getPattern());

		if (apiDeclaration == null)
		{
			apiDeclaration = new ApiDeclaration(route);
			addApi(apiDeclaration);
		}

		ApiOperation operation = new ApiOperation(route);
		apiDeclaration.operation(operation);
		return operation;
	}

	public void addModels(ApiOperation operation, Route route)
	{
		ModelResolver resolver = new ModelResolver(models);
		DataType returnType = resolver.resolve(route.getAction().getGenericReturnType());

		if (returnType.getRef() != null)
		{
			operation.setType(returnType.getRef());
		}
		else
		{
			operation.setType(returnType.getType());
			operation.setItems(returnType.getItems());
		}

		ApiModelRequest apiModelRequest = route.getAction().getAnnotation(
		    ApiModelRequest.class);

		if (apiModelRequest != null)
		{
			DataType bodyType = resolver.resolve(apiModelRequest.model(), apiModelRequest.modelName());
			operation.addParameter(new ApiOperationParameters("body", "body",
			    bodyType.getRef() != null ? bodyType.getRef() : bodyType
			        .getType(), apiModelRequest.required()));
		}
	}
}
