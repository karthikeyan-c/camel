package com.kc.camel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@ToString
public class Customer {
    String custId;
    String custName;

    @JsonIgnore
    Map<String,Customer> customerMap = new HashMap<>();

    public Customer addElement(Customer element) {
        customerMap.put(element.getCustId(),element);
        return element;
    }

    public Customer getElement(String custId) {
        return customerMap.get(custId);
    }

    public List<Customer> showElements() {
        return new ArrayList<>(customerMap.values());
    }
}
