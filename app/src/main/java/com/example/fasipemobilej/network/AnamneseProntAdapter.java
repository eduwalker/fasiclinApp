package com.example.fasipemobilej.network;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fasipemobilej.R;
import com.example.fasipemobilej.model.response.AnamneseResponse;

import java.util.List;

public class AnamneseProntAdapter extends RecyclerView.Adapter<AnamneseProntAdapter.ViewHolder> {
    private List<AnamneseResponse> anamneses;
    private ItemClickListener mClickListener;

    public AnamneseProntAdapter(List<AnamneseResponse> anamneses, ItemClickListener listener) {
        this.anamneses = anamneses;
        this.mClickListener = listener;
    }

    public interface ItemClickListener {
        void onItemClick(AnamneseResponse anamnese);
    }

    public void updateData(List<AnamneseResponse> newAnamneses) {
        anamneses.clear();
        anamneses.addAll(newAnamneses);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cards_anamnese_pront, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AnamneseResponse anamnese = anamneses.get(position);
        holder.bind(anamnese, mClickListener);
    }

    @Override
    public int getItemCount() {
        return anamneses.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textDescricao, textCriador, textData, textStatus, textAuth;

        public ViewHolder(View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitulo);
            textDescricao = itemView.findViewById(R.id.textDescricao);
            textCriador = itemView.findViewById(R.id.textCriador);
            textData = itemView.findViewById(R.id.textData);
            textStatus = itemView.findViewById(R.id.textStatus);
            textAuth = itemView.findViewById(R.id.textVisu);
        }

        @SuppressLint("SetTextI18n")
        public void bind(AnamneseResponse anamnese, ItemClickListener listener) {
            textTitle.setText("Paciente: " + anamnese.pacienteResponseDTO().nome_pac());
            textDescricao.setText("CPF: " + formatarCPF(anamnese.pacienteResponseDTO().cpf_pac()));
            textCriador.setText("Criador: " + anamnese.nomeProf());
            textAuth.setText("Visualização: " + (anamnese.authPac() == 1 ? "Autorizada" : "Não autorizada"));
            textData.setText("Data: " + DateFormatter.formatDateTime(anamnese.dataAnamnese().toString()));
            textStatus.setText("Status: " + anamnese.statusAnamneseFn());

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(anamnese);
            });
        }
    }

    public static String formatarCPF(String cpf) {
        return cpf.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
    }
}
