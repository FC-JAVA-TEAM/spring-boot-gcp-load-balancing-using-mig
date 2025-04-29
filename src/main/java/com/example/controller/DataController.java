package main.java.com.example.controller;

import com.github.javafaker.Faker;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;

@RestController
@RequestMapping("/api/data")
public class DataController {

    private final Faker faker = new Faker();

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    @GetMapping("/user")
    public Map<String, Object> getUser() {
        Map<String, Object> user = new HashMap<>();
        user.put("id", faker.number().randomNumber());
        user.put("username", faker.name().username());
        user.put("fullName", faker.name().fullName());
        user.put("email", faker.internet().emailAddress());
        user.put("phone", faker.phoneNumber().cellPhone());
        user.put("dateOfBirth", faker.date().birthday());
        return user;
    }

    @GetMapping("/users")
    public List<Map<String, Object>> getUsers() {
        List<Map<String, Object>> users = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            users.add(getUser());
        }
        return users;
    }

    @GetMapping("/product")
    public Map<String, Object> getProduct() {
        Map<String, Object> product = new HashMap<>();
        product.put("id", faker.number().randomNumber());
        product.put("name", faker.commerce().productName());
        product.put("price", faker.commerce().price());
        product.put("department", faker.commerce().department());
        product.put("material", faker.commerce().material());
        product.put("color", faker.commerce().color());
        return product;
    }

    @GetMapping("/products")
    public List<Map<String, Object>> getProducts() {
        List<Map<String, Object>> products = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            products.add(getProduct());
        }
        return products;
    }

    @GetMapping("/company")
    public Map<String, Object> getCompany() {
        Map<String, Object> company = new HashMap<>();
        company.put("name", faker.company().name());
        company.put("catchPhrase", faker.company().catchPhrase());
        company.put("industry", faker.company().industry());
        company.put("buzzword", faker.company().buzzword());
        company.put("logo", faker.company().logo());
        return company;
    }

    @GetMapping("/address")
    public Map<String, Object> getAddress() {
        Map<String, Object> address = new HashMap<>();
        address.put("streetAddress", faker.address().streetAddress());
        address.put("city", faker.address().city());
        address.put("state", faker.address().state());
        address.put("country", faker.address().country());
        address.put("zipCode", faker.address().zipCode());
        address.put("timeZone", faker.address().timeZone());
        return address;
    }

    @GetMapping("/bank")
    public Map<String, Object> getBankAccount() {
        Map<String, Object> bank = new HashMap<>();
        bank.put("accountNumber", faker.finance().iban());
        bank.put("accountName", faker.name().fullName());
        bank.put("currency", faker.currency().name());
        bank.put("creditCard", faker.business().creditCardNumber());
        bank.put("bic", faker.finance().bic());
        return bank;
    }
}
