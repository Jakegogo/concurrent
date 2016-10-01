package test.com.xuanwu.dw.transaction;

import java.sql.Connection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
// @TransactionConfiguration(transactionManager = "txManager", defaultRollback =
// false)
// @Transactional
// @TestExecutionListeners(listeners={
// DependencyInjectionTestExecutionListener.class,
// TransactionalTestExecutionListener.class
// })
@Component
public class SpringTransactionTest1 {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	@Qualifier("txManager")
	private PlatformTransactionManager transactionManager;
	
//	def.setIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ);
//	int num = jdbcTemplate.queryForInt("select num from test where id = 1");


	public void testA() throws InterruptedException {
		Thread.sleep(1000);// 1s
		
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		TransactionStatus status = transactionManager.getTransaction(def);
		
		try {
			int num = jdbcTemplate.queryForInt("select num from test where id = 1");



			jdbcTemplate.update("update test set num = 100 where id = 1");
			
			Thread.sleep(2000);// 4s
			transactionManager.commit(status);
		} catch (Exception e) {
			transactionManager.rollback(status);
			throw e;
		}
	}

	public void testB() throws InterruptedException {
		// 0s
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		TransactionStatus status = transactionManager.getTransaction(def);
		try {
			int num = jdbcTemplate.queryForInt("select num from test where id = 1");

			jdbcTemplate.update("update test set num = 200 where id = 1");
			
			transactionManager.commit(status);
		} catch (Exception e) {
			transactionManager.rollback(status);
			throw e;
		}
	}
	
	@Test
	public void testC() {
		Thread t1 = new Thread() {
			@Override
			public void run() {
				try {
					testA();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		Thread t2 = new Thread() {
			@Override
			public void run() {
				try {
					testB();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		t1.start();
		t2.start();
//		try {
//			t1.join();
//			t2.join();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// 输出结果
		int num = jdbcTemplate
				.queryForInt("select num from test where id = 1");
		System.out.println("result is " + num);
		
	}

}
