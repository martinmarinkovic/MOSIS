package com.martinmarinkovic.partytime;

// MyPlacesData - klasa koja će držati instance klasa MyPlace za svaku lokaciju koju budemo kreirali.
// 1. Klasa treba da sadrži privatno polje (objekat) generičke liste ArrayList<MyPlace> u koju ćemo smeštati kreirane instance MyPlace klase.
// 2. Pošto ćemo ovoj klasi pristupati iz različitih klasa projekta a želimo da nam se ključni podaci nalaze na jednom mestu, dobro bi bilo
// ovu klasu implementirati prema Singleton paternu. Vodićemo računa da implementirana klasa bude Thread Safe Signleton
// 3. Implementirati i metode za pristup celoj listi, pristup elementu liste, dodavanje novog elementa u listu i brisanje elementa iz liste.

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

}