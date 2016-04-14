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
public class SpringTransactionTest {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	@Qualifier("txManager")
	private PlatformTransactionManager transactionManager;
	
	
//	int num = jdbcTemplate.queryForInt("select num from test where id = 1");


	@Test
	@SuppressWarnings("deprecation")
	public void testA() throws InterruptedException {
		
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ);
		TransactionStatus status = transactionManager.getTransaction(def);
		
		try {
			int num = jdbcTemplate.queryForInt("select num from test where id = 1");
//			Thread.sleep(10);
			
			System.out.println(jdbcTemplate.queryForInt("select count(*) from test"));
			
			jdbcTemplate.update("update test set num = " + (num + 1) + " where id = 1");
			transactionManager.commit(status);
		} catch (Exception e) {
			transactionManager.rollback(status);
			throw e;
		}
	}

	
	@SuppressWarnings("deprecation")
	public void testB(int id) throws InterruptedException {
		// 0s
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ);
		TransactionStatus status = transactionManager.getTransaction(def);
		try {
			int num = jdbcTemplate.queryForInt("select num from test where id = 1");
//			Thread.sleep(10);
			
			jdbcTemplate.update("update test set num = " + (num + 1) + " where id = 1");
//			jdbcTemplate.update("insert into test values(" + id + ",'23',5)");
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
					for (int i = 0; i< 100;i++) {
						testA();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		Thread t2 = new Thread() {
			@Override
			public void run() {
				try {
					for (int i = 0; i< 100;i++) {
						testB(i + 3);
					}
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
			Thread.sleep(1000000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// 输出结果
		int num = jdbcTemplate
				.queryForInt("select num from test where id = 1");
		System.out.println("result is " + num);
		
	}
	
	
	
	@Test
	@Transactional
	@SuppressWarnings("deprecation")
	public void testD() throws InterruptedException {
		int num = jdbcTemplate.queryForInt("select num from test where id = 1");
		
		jdbcTemplate.update("update test set num = " + (num + 1) + " where id = 1");
//		throw new RuntimeException("测试回滚");
	}

}
