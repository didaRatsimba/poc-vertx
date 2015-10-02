package org.front.sample.poc;

import java.util.Arrays;

import com.google.common.net.MediaType;
import com.wylog.configs.utils.sample.poc.ConfigVerticle;
import com.wylog.pushserver.device.sample.poc.DevicePOCVerticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;


public class MainFrontVerticle extends AbstractVerticle {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void start() throws Exception {
		deployVerticles(new AbstractVerticle[]{new ConfigVerticle(),//
				new DevicePOCVerticle()});
		createServer();
	}

	private void deployVerticles(AbstractVerticle ... verticles){
		Arrays.asList(verticles).forEach(action -> {
			vertx.deployVerticle(action, handler -> {
				if(handler.succeeded()){
					logger.info("deployement succeded : ".concat(action.getClass().getName()));
					vertx.getOrCreateContext().put(action.getClass().getName(), handler.result());
				}
				else{
					logger.error("deployement failed : ".concat(action.getClass().getName()));
					logger.error(handler.cause());
				}
			});
		});

	}

	private void createServer(){
		Router router = Router.router(vertx);
		vertx.createHttpServer().requestHandler(router::accept).listen(8080);
		dispatchRequests(router);
	}

	private void dispatchRequests(Router router){
		router.route().handler(BodyHandler.create());

		router.post("/push-server/devices/:deviceId")//
		.consumes(ConfigVerticle.consumesMediaType(MediaType.JSON_UTF_8))//
		.produces(ConfigVerticle.producesMediatype(MediaType.JSON_UTF_8))//
		.handler(this::handleDevice)//
		;

		router.get("/").handler(this::handleRoot)//
		;

	}

	private void handleRoot(RoutingContext routingContext){
		HttpServerResponse response = routingContext.response();
		response.putHeader("content-type", "text/plain");
		response.setStatusCode(200);
		response.end("Hello World from Vert.x-Web!");
	}

	private void handleDevice(RoutingContext routingContext){

		String deviceId = routingContext.request().getParam("deviceId");
		HttpServerResponse response = routingContext.response();
		if (deviceId == null) {
			logger.error("received null deviceId");
			sendError(400, response);
		} else {
			logger.info("received deviceId ".concat(deviceId));
			JsonObject deviceJsonObject = new JsonObject().put(ConfigVerticle.DEVICE_KEY, deviceId);
			vertx.eventBus().send(ConfigVerticle.DEVICE_BUS_ADDRESS, deviceJsonObject, replyHandler -> {
				if(replyHandler.succeeded()){
					if(replyHandler.result() != null){
						if(replyHandler.result().body() instanceof JsonObject) {
							logger.info("replyHandler.result().body() instanceof JsonObject");
							JsonObject bodyReply = (JsonObject) replyHandler.result().body();
							logger.info("my body reply : ".concat(bodyReply.toString()));
							response.setStatusCode(200);
							response.putHeader("content-type", ConfigVerticle.producesMediatype(MediaType.JSON_UTF_8)).end(Buffer.buffer().appendString(bodyReply.encodePrettily()));
						}
						else{
							logger.error("body ".concat(replyHandler.result().body() != null ? replyHandler.result().body().getClass().getSimpleName() : null));
							sendError(500, response);
						}
					}
					else{
						logger.error("result null");
						sendError(500, response);
					}
				}
				else{
					logger.error("replyHandler failed");
					sendError(404, response);
				}
			});
		}
	}

	private void sendError(int statusCode, HttpServerResponse response) {
		response.setStatusCode(statusCode).end();
	}
}
