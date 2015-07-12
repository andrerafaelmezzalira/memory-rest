package com.memoryrest.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.memoryrest.memory.Prevayler;
import com.memoryrest.memory.Query;
import com.memoryrest.memory.TransactionWithQuery;

@Path("/example")
public class ExampleRest implements Serializable {

	private static final long serialVersionUID = 1L;
	static Prevayler prevayler;

	static {
		try {
			prevayler = new Prevayler();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(final @Context HttpServletRequest request)
			throws Exception {

		Object obj = prevayler.execute(new Query() {

			public StringBuilder query(StringBuilder prevalentSystem)
					throws Exception {

				boolean noParams = request.getParameterMap().size() == 0;

				if (noParams)
					throw new Exception();

				StringBuilder returns = new StringBuilder();

				Entry<String, String[]> entry = request.getParameterMap()
						.entrySet().iterator().next();
				String key = entry.getKey();
				// params is regexp or json
				if ("json".equals(key)) {
					String json = entry.getValue()[0];

					StringBuilder hasg = new StringBuilder(json.substring(1,
							json.indexOf(Constants.DOUBLE_QUOTES
									.concat(Constants.TWO_POINTS))));
					final Matcher matcher = Pattern.compile(
							"(.+?:(\\[.*?\\](,|$)|[^\\[]*?(,|$)))")
							.matcher(
									json.toString()
											.replaceFirst(
													hasg.toString() + ":", "")
											.replace('{', ' ')
											.replace('}', ' ').trim());

					final ArrayList<String> attributes = new ArrayList<String>();

					while (matcher.find())
						attributes.add(matcher.group());

					System.err.println(attributes);
				} else if ("regexp".equals(key)) {
					String regexp = entry.getValue()[0];
					Matcher matcher = Pattern.compile(regexp).matcher(
							prevalentSystem);
					while (matcher.find()) {
						returns.append(matcher.group());
					}
				} else {
					throw new Exception();
				}

				return returns;
			}
		});

		// System.err.println(obj);

		return Response.ok(obj).build();

	}

	static final StringBuilder params(InputStream inputStream) throws Exception {

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputStream));
		final StringBuilder params = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null)
			params.append(line);
		inputStream = null;
		reader = null;
		line = null;

		return params;
	}

	static final StringBuilder postTransaction(final StringBuilder json)
			throws Exception {
		StringBuilder returns = prevayler.execute(new TransactionWithQuery() {

			private static final long serialVersionUID = 1L;

			public final StringBuilder executeAndQuery(
					final StringBuilder prevalentSystem) throws Exception {

				Matcher matcher = Pattern.compile("(\"[^\"]+)")
						.matcher(json);
				final StringBuffer sb = new StringBuffer();
				while (matcher.find()) {
					matcher.appendReplacement(sb, matcher.group(1) + String.valueOf(json.hashCode()));
				}
				matcher.appendTail(sb);
				System.err.println(sb.toString());

				final StringBuilder record = new StringBuilder();
				String table = json.substring(1, json.indexOf("\":"));
				StringBuilder b = new StringBuilder(new StringBuilder()
						.append(table).append(json.hashCode()).append("\""));
				record.append("{").append(b).append(":")
						.append(json.substring(json.indexOf(":") + 1));

				prevalentSystem
						.append(prevalentSystem.length() == 0 ? "" : ",")
						.append(record);

				return record;
			}
		});
		return returns;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response post(final @Context HttpServletRequest request)
			throws Exception {

		StringBuilder params = params(request.getInputStream());

		StringBuilder returns = postTransaction(params);

		// System.err.println(returns);

		return Response.ok(returns).build();
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response put(final @Context HttpServletRequest request)
			throws Exception {

		// comeca aqui
		final StringBuilder json = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				request.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null)
			json.append(line);

		reader = null;
		line = null;

		Object obj = prevayler.execute(new TransactionWithQuery() {

			private static final long serialVersionUID = 1L;

			public final StringBuilder executeAndQuery(
					final StringBuilder prevalentSystem) throws Exception {

				return null;
			}
		});

		System.err.println(obj);

		return Response.ok(obj).build();
	}

}