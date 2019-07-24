package com.martinmarinkovic.partytime;

// MyPlacesData - klasa koja će držati instance klasa MyPlace za svaku lokaciju koju budemo kreirali.
// 1. Klasa treba da sadrži privatno polje (objekat) generičke liste ArrayList<MyPlace> u koju ćemo smeštati kreirane instance MyPlace klase.
// 2. Pošto ćemo ovoj klasi pristupati iz različitih klasa projekta a želimo da nam se ključni podaci nalaze na jednom mestu, dobro bi bilo
// ovu klasu implementirati prema Singleton paternu. Vodićemo računa da implementirana klasa bude Thread Safe Signleton
// 3. Implementirati i metode za pristup celoj listi, pristup elementu liste, dodavanje novog elementa u listu i brisanje elementa iz liste.

// Da bi bila dodata podrška za rad sa Firebase bazom podataka, potrebno je izvršiti i mapiranje različitih domena ključeva. Firebase baza podataka
// svakom smeštenom objektu dodeljuje ključ, tipa string, koji zavisi od vremena dodavanja objekta. Model podataka na kome se zasniva MyPlaces aplikacija
// je lista čijim elementima se pristupa preko indeksa. Pošto se kao identifikatori instanci MyPlace klase u aplikaciji koriste celi brojevi, njihovi
// indeksi u listi, a kako baza koristi stringove, potrebno je izvršiti i mapiranje ovih ključeva. Kako je prilikom promene vrednosti atributa instance
// MyPlace klase dostupan indeks a na osnovu njega i ključ (preko atributa klase MyPlace), problem predstavlja mapiranje ključa u indeks. Ovo mapiranje
// je potrebno kako bi moglo da se pristupi odgovarajućem elementu na osnovu indeksa u slučaju kada je poznat samo ključ. Pored toga, ovakvo mapiranje
// služi i za brzu proveru da li je podatak dostupan ili ne u aplikaciji.
// Kako bi aplikacija mogla da prati promene nad podacima i da ih isčitava, potrebno je registrovati odgovarajuće event listenere koji se aktiviraju prilikom
// inicijalnog učitavanja iz baze podataka kao i prilikom promena koje su izvršene nad podacima. Pošto je osnovni model podataka aplikacije lista, u tom cilju
// je potrebno dodati ChildEventListener i SingleValueListener referenci baze podataka kako bi promene bile ispravno praćene.

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;

public class MyPlacesData {

    private ArrayList<MyPlace> myPlaces;
    private HashMap<String, Integer> myPlacesKeyIndexMapping;
    private DatabaseReference database;
    private static final String FIREBASE_CHILD = "my-places";

    private MyPlacesData() {
        myPlaces = new ArrayList<MyPlace>();
        myPlacesKeyIndexMapping = new HashMap<String, Integer>();
        database = FirebaseDatabase.getInstance().getReference();
        database.child(FIREBASE_CHILD).addChildEventListener(childEventListener);
        database.child(FIREBASE_CHILD).addListenerForSingleValueEvent(parentEventListener);
    }

    ValueEventListener parentEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (updatedEventListener != null)
                updatedEventListener.onListUpdated();
        }
        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
        }
    };

    ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            String myPlaceKey = dataSnapshot.getKey();
            if (!myPlacesKeyIndexMapping.containsKey(myPlaceKey)) {
                MyPlace myPlace = dataSnapshot.getValue(MyPlace.class);
                myPlace.key = myPlaceKey;
                myPlaces.add(myPlace);
                myPlacesKeyIndexMapping.put(myPlaceKey, myPlaces.size()-1);

                if (updatedEventListener != null)
                    updatedEventListener.onListUpdated();
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            String myPlaceKey = dataSnapshot.getKey();
            MyPlace myPlace = dataSnapshot.getValue(MyPlace.class);
            myPlace.key = myPlaceKey;
            if (myPlacesKeyIndexMapping.containsKey(myPlaceKey)) {
                int index = myPlacesKeyIndexMapping.get(myPlaceKey);
                myPlaces.set(index, myPlace);
            } else {
                myPlaces.add(myPlace);
                myPlacesKeyIndexMapping.put(myPlaceKey, myPlaces.size()-1);
            }
            if (updatedEventListener != null)
                updatedEventListener.onListUpdated();
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            String myPlaceKey = dataSnapshot.getKey();
            if (myPlacesKeyIndexMapping.containsKey(myPlaceKey)) {
                int index = myPlacesKeyIndexMapping.get(myPlaceKey);
                myPlaces.remove(index);
                recreateKeyIndexMapping();

                if (updatedEventListener != null)
                    updatedEventListener.onListUpdated();
            }
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
        }
    };

    private static class SingletonHolder {
        public static final MyPlacesData instance = new MyPlacesData();
    }

    public static MyPlacesData getInstance() {
        return SingletonHolder.instance;
    }

    public ArrayList<MyPlace> getMyPlaces() {
        return myPlaces;
    }

    public void addNewPlace(MyPlace place) {
        String key = database.push().getKey();
        myPlaces.add(place);
        myPlacesKeyIndexMapping.put(key, myPlaces.size() - 1);
        database.child(FIREBASE_CHILD).child(key).setValue(place);
        place.key = key;
    }

    public MyPlace getPlace(int index) {
        return myPlaces.get(index);
    }

    public void deletePlace(int index) {
        database.child(FIREBASE_CHILD).child(myPlaces.get(index).key).removeValue();
        myPlaces.remove(index);
        recreateKeyIndexMapping();
    }

    public void updatePlace(int index, String name, String desc, String lon, String lat) {
        MyPlace myPlace = myPlaces.get(index);
        myPlace.name = name;
        myPlace.description = desc;
        myPlace.latitude = lat;
        myPlace.longitude = lon;
        database.child(FIREBASE_CHILD).child(myPlace.key).setValue(myPlace);
    }

    public void updatePlace(int index, String name, String desc) {
        MyPlace myPlace = myPlaces.get(index);
        myPlace.name = name;
        myPlace.description = desc;
        database.child(FIREBASE_CHILD).child(myPlace.key).setValue(myPlace);
    }

    public void recreateKeyIndexMapping() {
        myPlacesKeyIndexMapping.clear();
        for (int i = 0; i < myPlaces.size(); i++)
            myPlacesKeyIndexMapping.put(myPlaces.get(i).key, i);
    }

    ListUpdatedEventListener updatedEventListener;
    public void setParentEventListener(ListUpdatedEventListener listener) {
        updatedEventListener = listener;
    }

    public interface ListUpdatedEventListener {
        void onListUpdated();
    }
}

