package com.example.loomservlet;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jetty.util.VirtualThreads;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class ServletOfTheLoomApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServletOfTheLoomApplication.class, args);
	}

	@Bean
	AsyncTaskExecutor applicationTaskExecutor() {
		// enable async servlet support
		ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
		return new TaskExecutorAdapter(executorService::execute);
	}

	@Bean
	JettyServletWebServerFactory JettyServletWebServerFactory(
			ObjectProvider<JettyServerCustomizer> serverCustomizers) {
		JettyServletWebServerFactory factory = new JettyServletWebServerFactory() {
			@Override
			public void setThreadPool(ThreadPool threadPool) {
				if (threadPool instanceof VirtualThreads.Configurable configurable) {
					configurable.setUseVirtualThreads(true);
				}
				super.setThreadPool(threadPool);
			}
		};
		factory.getServerCustomizers().addAll(serverCustomizers.orderedStream().toList());
		return factory;
	}

	@RestController
	static class MyController {

		final JdbcTemplate jdbcTemplate;

		MyController(JdbcTemplate jdbcTemplate) {
			this.jdbcTemplate = jdbcTemplate;
		}

		@GetMapping("/")
		String getValue() throws InterruptedException {

			// Simulate a blocking call for one second. The thread should be put aside for about a second.
			Thread.sleep(1000);
			return "OK";
		}

		@GetMapping("/where-am-i")
		String getThreadName() {
			return Thread.currentThread().toString();
		}

		@GetMapping("/where-am-i-async")
		Callable<String> getAsyncThreadName() {
			return () -> Thread.currentThread().toString();
		}

		@GetMapping("/sql")
		String getFromDatabase() {

			// Simulate blocking I/O where the server side controls the timeout. The thread should be put aside for about a second.
			return jdbcTemplate.queryForList("select pg_sleep(1);").toString();
		}
	}

}
