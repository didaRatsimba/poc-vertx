package com.wylog.pushserver.device.sample.poc;

import com.wylog.configs.utils.sample.poc.ConfigVerticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;


public class DevicePOCVerticle extends AbstractVerticle {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private String result = null;


	@Override
	public void start() throws Exception {
		vertx.eventBus().consumer(ConfigVerticle.DEVICE_BUS_ADDRESS, handler -> {
			Object messageBody = null;
			logger.debug(handler.body().getClass());
			if(handler.body() instanceof String){
				messageBody = handler.body();
				logger.info(messageBody);
			}
			else if(handler.body() instanceof JsonObject){
				messageBody = handler.body();
				vertx.runOnContext(save((JsonObject) messageBody, handler));
				String body = ((JsonObject) messageBody).getString(ConfigVerticle.DEVICE_KEY, "default-value-if-not-present");
				logger.info(body.concat(" received in DevicePOCVerticle" ));
			}
			else{
				logger.error("neither a String nor JsonObject");
			}
		});
	}


	private void saveData(JsonObject data, Message<Object> handler){
		//TODO : find a way to load these configurations from classpath
		JsonObject config = new JsonObject().put("connection_string", "mongodb://localhost:27017").put("db_name","test");
		//using vertx-mongoclient 
		MongoClient client = MongoClient.createShared(vertx, config);
		if(config.isEmpty()){
			logger.warn("config is empty now");
		}
		else{
			config.forEach(consumer -> {
				logger.info("key :".concat(consumer.getKey()));
				logger.info("value :".concat(consumer.getValue() != null ? consumer.getValue().toString() : null));
			});
		}
		if(client != null){
			data.forEach(datas -> {
				logger.info("data encoded ".concat(data.encodePrettily()));
			});
			client.save("mariahcarrey", data, resultHandler -> {

				if (resultHandler.succeeded()) {

					result = resultHandler.result();
					logger.info("Saved data with id " + result);
					JsonObject eventBusReplyJsonObject = new JsonObject().put(ConfigVerticle.DEVICE_KEY, result);
					handler.reply(eventBusReplyJsonObject);
				} else {
					logger.error(resultHandler.cause().getMessage());
				}
			});
		}
	}

	private Handler<Void> save(JsonObject data, Message<Object> handler){

		return event -> {
			logger.info("event class : ".concat(event != null ? event.getClass().toString() : "null event"));
			saveData(data, handler);
		};
	}
}
