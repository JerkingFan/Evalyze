package org.example.new_new_mvp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;

@SpringBootApplication(exclude = {
    HibernateJpaAutoConfiguration.class,
    DataSourceAutoConfiguration.class,
    JpaRepositoriesAutoConfiguration.class
})
public class NewNewMvpApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewNewMvpApplication.class, args);
    }

}
