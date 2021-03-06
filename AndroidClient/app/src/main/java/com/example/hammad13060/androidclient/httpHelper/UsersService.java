package com.example.hammad13060.androidclient.httpHelper;

import com.example.hammad13060.androidclient.httpHelper.busEvents.DeleteUserEvent;
import com.example.hammad13060.androidclient.httpHelper.busEvents.GetAllUsersEvent;
import com.example.hammad13060.androidclient.httpHelper.busEvents.UserEvent;
import com.example.hammad13060.androidclient.httpHelper.entities.User;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

/**
 * Created by hammad13060 on 27/04/16.
 */
public class UsersService {

    private static UsersService usersService = new UsersService();

    private static final String SERVER_URL = "http://192.168.0.8:3000/";

    private static UsersServiceInterface service = new Retrofit.Builder()
            .baseUrl(SERVER_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(UsersServiceInterface.class);

    private static EventBus eventBus = EventBus.getDefault();

    public static final int STATUS_FAILED = 0;
    public static final int STATUS_SUCCESS = 1;

    private UsersService() {}

    public static UsersService getInstance() {
        return  usersService;
    }

    public void getAllUsers() {
        service.getAllUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.code() == 200) {
                    eventBus.post(new GetAllUsersEvent(STATUS_SUCCESS, response.body()));
                } else {
                    eventBus.post(new GetAllUsersEvent(STATUS_FAILED, new ArrayList<User>()));
                }
                call.cancel();
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                eventBus.post(new GetAllUsersEvent(STATUS_FAILED, new ArrayList<User>()));
                call.cancel();
            }
        });
    }

    public void getUser(int id) {
        service.getUser(id).enqueue(initUserCallback(200));
    }

    public void addUser(User user) {
        service.addUser(user).enqueue(initUserCallback(201));
    }

    public void deleteUser(int id) {
        service.deleteUser(id).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call call, Response response) {
                if (response.code() == 200) {
                    eventBus.post(new DeleteUserEvent(STATUS_SUCCESS));
                } else {
                    eventBus.post(new DeleteUserEvent(STATUS_FAILED));
                }
                call.cancel();
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                eventBus.post(new DeleteUserEvent(STATUS_FAILED));
                call.cancel();
            }
        });
    }

    public void updateUser(int id, User user) {
        service.updateUser(id, user).enqueue(initUserCallback(200));
    }

    public Callback<User> initUserCallback(final int expectedCode) {
        return new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.code() == expectedCode) {
                    eventBus.post(new UserEvent(STATUS_SUCCESS, response.body()));
                } else {
                    eventBus.post(new UserEvent(STATUS_FAILED, new User("invalid", "invalid")));
                }
                call.cancel();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                eventBus.post(new UserEvent(STATUS_FAILED, new User("invalid", "invalid")));
                call.cancel();
            }
        };
    }
}
