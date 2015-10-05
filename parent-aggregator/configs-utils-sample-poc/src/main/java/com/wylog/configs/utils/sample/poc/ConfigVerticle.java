package com.wylog.configs.utils.sample.poc;

import com.google.common.net.MediaType;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;


public class ConfigVerticle extends AbstractVerticle {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public static final String DEVICE_BUS_ADDRESS = "com.wylog.device.bus.address";

	public static final int PORT = 8080;

	public static final String DEVICE_KEY = "device_id";

	public static final String acceptMediaType(MediaType mediatype){
		return mediatype.type().concat("/").concat(mediatype.subtype());
	}

	public static final String consumesMediaType(MediaType mediatype){
		return "application".concat("/").concat(mediatype.subtype());
	}

	public static final String producesMediatype(MediaType mediatype){
		return "application".concat("/").concat(mediatype.subtype());
	}

	@Override
	public void start() throws Exception {
		logger.info("config verticle loaded");
	}
}
