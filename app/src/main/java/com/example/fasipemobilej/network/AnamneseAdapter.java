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

public class AnamneseAdapter extends RecyclerView.Adapter<AnamneseAdapter.ViewHolder> {
    private List<AnamneseResponse> anamneses;
    private ItemClickListener mClickListener;

    public AnamneseAdapter(List<AnamneseResponse> anamneses, ItemClickListener listener) {
        this.anamneses = anamneses;
        this.mClickListener = listener;
    }

    public interface ItemClickListener {
        void onItemClick(AnamneseResponse anamnese);
    }

    public void updateData(List<AnamneseResponse> newAnamneses) {
        if (newAnamneses != null) {
            anamneses.clear();
            anamneses.addAll(newAnamneses);
            notifyDataSetChanged();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cards_anamnese, parent, false);
        return new ViewHolder(view, mClickListener);
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
        TextView textTitle, textDescricao, textData, textStatus;
        ItemClickListener mListener;

        public ViewHolder(View itemView, ItemClickListener listener) {
            super(itemView);
            this.mListener = listener;
            textTitle = itemView.findViewById(R.id.textTitulo);
            textDescricao = itemView.findViewById(R.id.textDescricao);
            textData = itemView.findViewById(R.id.textData);
            textStatus = itemView.findViewById(R.id.textStatus);
        }

        @SuppressLint("SetTextI18n")
        public void bind(AnamneseResponse anamnese, ItemClickListener listener) {
            textTitle.setText("Nome: " + anamnese.pacienteResponseDTO().nome_pac());
            textDescricao.setText("CPF: " + formatarCPF(anamnese.pacienteResponseDTO().cpf_pac()));
            textData.setText("Data: " + DateFormatter.formatDateTime(anamnese.dataAnamnese().toString()));
            textStatus.setText("Status: " + anamnese.statusAnamneseFn());

            // Verificação de null para statusAnamneseFn
            String status = anamnese.statusAnamneseFn();
            if (status != null) {
                // Mudando a cor da borda conforme o status
                switch (status) {
                    case "Cancelada":
                        itemView.setBackgroundResource(R.drawable.border_red);
                        break;
                    case "Analise":
                        itemView.setBackgroundResource(R.drawable.border_blue);
                        break;
                    case "Reprovada":
                        itemView.setBackgroundResource(R.drawable.border_orange);
                        break;
                    case "Aprovada":
                        itemView.setBackgroundResource(R.drawable.border_green);
                        break;
                    default:
                        itemView.setBackgroundResource(R.drawable.background_cards); // Cor padrão
                        break;
                }
            } else {
                // Caso o status seja null, definir um background padrão
                itemView.setBackgroundResource(R.drawable.background_cards);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(anamnese);
            });
        }
    }

    public static String formatarCPF(String cpf) {
        return cpf.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
    }
}
