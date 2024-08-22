package io.mosip.kernel.emailnotification.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.core.templatemanager.exception.TemplateMethodInvocationException;
import io.mosip.kernel.core.templatemanager.exception.TemplateParsingException;
import io.mosip.kernel.core.templatemanager.exception.TemplateResourceNotFoundException;
import io.mosip.kernel.core.templatemanager.spi.TemplateManager;
import io.mosip.kernel.emailnotification.constant.ApiName;
import io.mosip.kernel.emailnotification.constant.LoggerFileConstant;
import io.mosip.kernel.emailnotification.dto.ResponseWrapper;
import io.mosip.kernel.emailnotification.dto.TemplateResponseDto;
import io.mosip.kernel.emailnotification.exception.ApisResourceAccessException;
import io.mosip.kernel.emailnotification.exception.TemplateProcessingFailureException;
import io.mosip.kernel.emailnotification.exception.util.PlatformErrorMessages;
import io.mosip.kernel.emailnotification.service.PrintRestClientService;
import io.mosip.kernel.templatemanager.velocity.impl.TemplateManagerImpl;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.NullLogChute;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * The Class TemplateGenerator.
 * 
 * @author M1048358 Alok
 */
@Component
public class TemplateGenerator {

	/** The reg proc logger. */
	private static Logger notificationLogger = LoggerFactory.getLogger(TemplateGenerator.class);

	/** The resource loader. */
	private String resourceLoader = "classpath";

	/** The template path. */
	private String templatePath = ".";

	/** The cache. */
	private boolean cache = Boolean.TRUE;

	/** The default encoding. */
	private String defaultEncoding = StandardCharsets.UTF_8.name();

	/** The rest client service. */
	@Autowired
	private PrintRestClientService<Object> restClientService;

	@Autowired
	private ObjectMapper mapper;

	/**
	 * Gets the template.
	 *
	 * @param templateTypeCode
	 *            the template type code
	 * @param attributes
	 *            the attributes
	 * @param langCode
	 *            the lang code
	 * @return the template
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ApisResourceAccessException
	 *             the apis resource access exception
	 */
	public InputStream getTemplate(String templateTypeCode, Map<String, Object> attributes, String langCode)
			throws IOException, ApisResourceAccessException {

		ResponseWrapper<?> responseWrapper;
		TemplateResponseDto template;
		notificationLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), "",
				"TemplateGenerator::getTemplate()::entry");

		try {
			List<String> pathSegments = new ArrayList<>();
			pathSegments.add(langCode);
			pathSegments.add(templateTypeCode);
			notificationLogger.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), "",
				"Language:::::: "+langCode );
			notificationLogger.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), "",
				"templateTypeCode :::::::: "+templateTypeCode );
			responseWrapper = (ResponseWrapper<?>) restClientService.getApi(ApiName.TEMPLATES, pathSegments, "", "",
					ResponseWrapper.class);
			template = mapper.readValue(mapper.writeValueAsString(responseWrapper.getResponse()),
					TemplateResponseDto.class);

			InputStream fileTextStream = null;
			if (template != null) {
				InputStream stream = new ByteArrayInputStream(
						template.getTemplates().iterator().next().getFileText().getBytes());
				fileTextStream = getTemplateManager().merge(stream, attributes);
			}
			notificationLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), "",
					"TemplateGenerator::getTemplate()::exit");
			return fileTextStream;

		} catch (TemplateResourceNotFoundException | TemplateParsingException | TemplateMethodInvocationException e) {
			notificationLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					null, PlatformErrorMessages.RPR_TEM_PROCESSING_FAILURE.name() + e.getMessage()
							+ ExceptionUtils.getStackTrace(e));
			throw new TemplateProcessingFailureException(PlatformErrorMessages.RPR_TEM_PROCESSING_FAILURE.getCode());
		}
	}

	/**
	 * Gets the template manager.
	 *
	 * @return the template manager
	 */
	public TemplateManager getTemplateManager() {
		final Properties properties = new Properties();
		properties.put(RuntimeConstants.INPUT_ENCODING, defaultEncoding);
		properties.put(RuntimeConstants.OUTPUT_ENCODING, defaultEncoding);
		properties.put(RuntimeConstants.ENCODING_DEFAULT, defaultEncoding);
		properties.put(RuntimeConstants.RESOURCE_LOADER, resourceLoader);
		properties.put(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, templatePath);
		properties.put(RuntimeConstants.FILE_RESOURCE_LOADER_CACHE, cache);
		properties.put(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, NullLogChute.class.getName());
		properties.put("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		properties.put("file.resource.loader.class", FileResourceLoader.class.getName());
		VelocityEngine engine = new VelocityEngine(properties);
		engine.init();
		return new TemplateManagerImpl(engine);
	}
}
