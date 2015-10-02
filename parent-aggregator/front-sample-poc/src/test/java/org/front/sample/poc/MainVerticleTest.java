package org.front.sample.poc;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class MainVerticleTest {

	private Vertx vertx;

	@Before
	public void setUp(TestContext context) {
		Async async = context.async();
		vertx = Vertx.vertx();
		vertx.deployVerticle(MainFrontVerticle.class.getName(), ar -> {
			if (ar.succeeded()) {
				async.complete();
			} else {
				context.fail("Could not deploy verticle");
			}
		});
	}

	@Test
	public void testHello(TestContext context) {
		Async async = context.async();
		HttpClient client = vertx.createHttpClient();
		HttpClientRequest req = client.get(8080, "localhost", "/");
		req.exceptionHandler(err -> {
			context.fail();
		});
		req.handler(resp -> {
			context.assertEquals(200, resp.statusCode());
			Buffer entity = Buffer.buffer();
			resp.handler(entity::appendBuffer);
			resp.endHandler(v -> {
				context.assertEquals("Hello World from Vert.x-Web!", entity.toString("UTF-8"));
				async.complete();
			});
		});
		req.end();

		//		HttpClientRequest req2 = client.post(8080, "localhost", "/push-server/devices/");
		//		req2.exceptionHandler(err -> {
		//			context.fail(err);
		//		});
		//		req2.handler(resp -> {
		//			context.assertEquals(200, resp.statusCode());
		//			//			Buffer entity = Buffer.buffer();
		//			//			resp.handler(entity::appendBuffer);
		//			//			resp.endHandler(v -> {
		//			//				context.assertEquals("Hello World from Vert.x-Web!", entity.toString("UTF-8"));
		//			//				async.complete();
		//			//			});
		//		});
		//		req2.end();


	}

	@After
	public void tearDown(TestContext context) {
		Async async = context.async();
		vertx.close(ar -> {
			async.complete();
		});
	}
}
