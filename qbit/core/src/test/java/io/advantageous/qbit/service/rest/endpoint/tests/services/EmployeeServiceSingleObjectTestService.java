package io.advantageous.qbit.service.rest.endpoint.tests.services;

import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.annotation.RequestParam;
import io.advantageous.qbit.http.HttpStatusCodeException;
import io.advantageous.qbit.http.request.HttpResponseBuilder;
import io.advantageous.qbit.http.request.HttpTextResponse;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.service.rest.endpoint.tests.model.Employee;

import java.util.ArrayList;
import java.util.List;

import static io.advantageous.boon.core.IO.puts;

@RequestMapping("/es")
public class EmployeeServiceSingleObjectTestService {


    private final List<Employee> employeeList  = new ArrayList<>();


    @RequestMapping("/echo1")
    public String echoParamRequired(@RequestParam(value = "foo", required = true) String foo) {
        return foo;
    }

    @RequestMapping("/echo2")
    public String echoDefaultParam(@RequestParam(value = "foo", defaultValue = "mom") String foo) {
        return foo;
    }

    @RequestMapping("/echo3")
    public String echoException() {
        throw new HttpStatusCodeException(700, "Ouch!");

    }

    @RequestMapping("/echo4")
    public void echoException2(final Callback<String> callback) {
        callback.onError(HttpStatusCodeException.httpError(900, "Ouch!!"));
    }

    @RequestMapping("/echo5")
    public void echoException3(final Callback<String> callback) {

        try {

            throw new IllegalStateException("Shoot!!");
        }catch (Exception ex) {
            callback.onError(HttpStatusCodeException.httpError(666, ex.getMessage(), ex));

        }
    }


    @RequestMapping("/ping")
    public boolean ping() {
        return true;
    }
    /**
     * This is a fire and forget style.
     * There is no return and the client cannot get any exception that this might throw.
     *
     * @param employee employee
     */
    @RequestMapping(value = "/employee-add-async-no-return", method = RequestMethod.POST)
    public void addEmployee(final Employee employee) {
        employeeList.add(employee);
        puts(employee);
    }



    /**
     * There has a return and the client can get notified of exceptions that this might throw.
     *
     * @param employee employee
     */
    @RequestMapping(value = "/employee-ack", method = RequestMethod.POST)
    public boolean addEmployeeAck(final Employee employee) {
        puts(employee);

        employeeList.add(employee);
        return true;
    }


    /**
     * This has a return and now it can talk to downstream services or IO that has an async API.
     * In this example, we call the callback synchronously, but we could call the callback in
     * another thread of execution or call services that have async callbacks and call
     * this callback when the downstream async service returns.
     *
     * @param callback callback
     * @param employee employee
     */
    @RequestMapping(value = "/employee-async-ack", method = RequestMethod.POST)
    public void addEmployeeAsyncAck(final Callback<Boolean> callback,
                                    final Employee employee) {
        puts(employee);
        boolean add = employeeList.add(employee);
        callback.accept(add);
    }


    @RequestMapping(value = "/throw", method = RequestMethod.POST)
    public void addEmployeeThrowException(final Callback<Boolean> callback,
                                    final Employee employee) {
        puts(employee);
        throw new RuntimeException("OH NO");
    }



    @RequestMapping("/returnEmployee")
    public Employee returnEmployee() {

        return new Employee(1, "Rick");

    }

    @RequestMapping("/returnEmployeeCallback")
    public void returnEmployeeWithCallback(Callback<Employee> employeeCallback) {

        employeeCallback.returnThis(new Employee(1, "Rick"));

    }


    @RequestMapping("/returnEmployeeCallback2")
    public void returnEmployeeWithCallback2(Callback<HttpTextResponse> employeeCallback) {

        HttpTextResponse response = (HttpTextResponse) HttpResponseBuilder.httpResponseBuilder()
                .setBody("" + new Employee(1, "Rick"))
                .setContentType("crap/crap").setCode(777).build();

        employeeCallback.returnThis(response);

    }





}
