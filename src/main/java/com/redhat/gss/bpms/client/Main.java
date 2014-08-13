package com.redhat.gss.bpms.client;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.services.client.api.RemoteJmsRuntimeEngineFactory;

public class Main {

	static final String DEPLOYMENT_ID = "org.jugvale.tdc:tdc-ola:1.1";
	static final InitialContext CONTEXT = createInitialContext();
	static final String PROCESS_NAME = "tdc-ola.ola-mundo-tdc";
	static final String USERNAME = "jesuino";
	static final String PASSWORD = "redhat2014!";
	static final String JMS_URL = "remote://localhost:4447";
	static final String HTTP_URL = "http://localhost:8080/business-central";

	private static final int MAX_WAIT = 100000;

	public static void main(String[] args) throws MalformedURLException {
		jmsTest();
	}

	private static void jmsTest() {
		RemoteJmsRuntimeEngineFactory jmsRuntimeEngineFactory = new RemoteJmsRuntimeEngineFactory(
				DEPLOYMENT_ID, CONTEXT, USERNAME, PASSWORD, MAX_WAIT);

		RuntimeEngine engine = jmsRuntimeEngineFactory.newRuntimeEngine();
		KieSession ksession = engine.getKieSession();

		HashMap<String, Object> params = new HashMap<>();
		params.put("name", "William");
		// start a sample process
		ksession
				.startProcess(PROCESS_NAME, params);
		TaskService taskService = engine.getTaskService();
		taskService
				.getTasksAssignedAsPotentialOwner(USERNAME, "en-UK")
				.forEach(
						t -> {
							System.out.println("Tasks:");
							System.out.printf("%s: %d - %s - %s\n",
									t.getProcessId(), t.getId(), t.getName(),
									t.getStatus());
							if (t.getStatus().name().equals("InProgress")) {
								System.out.println("Completing " + t.getId());
								taskService.complete(t.getId(), USERNAME, null);
							}
							if (t.getStatus().name().equals("Reserved")) {
								System.out.println("Starting " + t.getId());
								taskService.start(t.getId(), USERNAME);
							}
						});
	}

	static private InitialContext createInitialContext() {

		Properties props = new Properties();
		props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY,
				"org.jboss.naming.remote.client.InitialContextFactory");
		props.setProperty(InitialContext.PROVIDER_URL, JMS_URL);
		props.setProperty(InitialContext.SECURITY_PRINCIPAL, USERNAME);
		props.setProperty(InitialContext.SECURITY_CREDENTIALS, PASSWORD);

		try {
			return new InitialContext(props);
		} catch (NamingException e) {
			throw new Error(e);

		}
	}

}
