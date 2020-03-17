package com.example.androidroomdatabase;

import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import android.view.View;
import android.widget.ListView;
import android.widget.ArrayAdapter;


import android.os.Bundle;
import android.widget.Toast;

import com.example.androidroomdatabase.Database.UserRepository;
import com.example.androidroomdatabase.Local.UserDataSource;
import com.example.androidroomdatabase.Local.UserDatabase;
import com.example.androidroomdatabase.Model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private ListView lstUser;
    private FloatingActionButton fab;

    //Adapter
    List<User> userList = new ArrayList<>();
    ArrayAdapter adapter;

    //Database
    private CompositeDisposable compositeDisposable;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Init
        compositeDisposable = new CompositeDisposable();


        //Init View
        lstUser = (ListView) findViewById(R.id.lstUsers);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, userList);
        registerForContextMenu(lstUser);
        lstUser.setAdapter(adapter);

        //Database
        UserDatabase userDatabase = UserDatabase.getInstance(this); //Create database
        userRepository = UserRepository.getInstance(UserDataSource.getInstance(userDatabase.userDAO()));

        //Load all data from Database
        loadData();

        //Event
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Add new user
                //Random email
                Disposable disposable = Observable.create(new ObservableOnSubscribe<Object>() {
                    @Override
                    public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                        User user = new User("André",
                                UUID.randomUUID().toString()+"@gmail.com");

                        userRepository.insertUser(user);
                        emitter.onComplete();
                    }
                })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Consumer() {
                                       @Override
                                       public void accept(Object o) throws Exception {
                                           Toast.makeText(MainActivity.this, "User added!!",
                                                   Toast.LENGTH_SHORT).show();
                                       }
                                   }, new Consumer<Throwable>() {
                                       @Override
                                       public void accept(Throwable throwable) throws Exception {
                                           Toast.makeText(MainActivity.this, "" + throwable.getMessage(),
                                                   Toast.LENGTH_SHORT).show();
                                       }
                                   },
                                new Action() {
                                    @Override
                                    public void run() throws Exception {
                                        loadData(); //Refresh data
                                    }
                                }

                        );
            }
        });
    }

    private void loadData() {
        //Use RxJava
        Disposable disposable = userRepository.getAllUsers()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<List<User>>() {
                    @Override
                    public void accept(List<User> users) throws Exception {
                        onGetAllUserSuccess(users);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(MainActivity.this, ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        compositeDisposable.add(disposable);
    }

    private void onGetAllUserSuccess(List<User> users) {
        userList.clear();
        userList.addAll(users);
        adapter.notifyDataSetChanged();
    }
}
