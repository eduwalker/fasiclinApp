package com.example.fasipemobilej;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class TelaQuestions extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        getSupportActionBar().hide();

        setupSpinnerSexo();
        setupSpinnerEstadoCivil();
        setupBackButton();
    }

    private void setupSpinnerSexo() {
        Spinner spinnerSexo = findViewById(R.id.spinnerSexo);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sexo_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSexo.setAdapter(adapter);

        // Listener omitido para brevidade
    }

    private void setupSpinnerEstadoCivil() {
        Spinner spinnerEstadoCivil = findViewById(R.id.spinnerEstadoCivil); // Substitua pelo ID correto do seu Spinner
        ArrayAdapter<CharSequence> adapterEstadoCivil = ArrayAdapter.createFromResource(this,
                R.array.estado_civil_array, android.R.layout.simple_spinner_item);
        adapterEstadoCivil.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstadoCivil.setAdapter(adapterEstadoCivil);

        // Configuração de listener opcional aqui
    }

    private void setupBackButton() {
        ImageButton backButton = findViewById(R.id.buttonReturn); // Substitua 'buttonReturn' pelo ID do seu botão de voltar
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Encerra a atividade
            }
        });
    }
}

