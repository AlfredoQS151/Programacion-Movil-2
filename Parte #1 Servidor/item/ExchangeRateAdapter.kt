package com.example.divisa_1.item

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.divisa_1.databinding.ItemExchangeRateBinding

// Adapter para el RecyclerView que muestra las tasas de cambio
class ExchangeRateAdapter(private val exchangeRates: List<ExchangeRateItem>) :
    RecyclerView.Adapter<ExchangeRateAdapter.ExchangeRateViewHolder>() {

    // Este método crea un ViewHolder que contiene la vista de cada item del RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExchangeRateViewHolder {
        // Inflamos el layout del item (ItemExchangeRateBinding) para cada celda
        val binding = ItemExchangeRateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        // Retornamos el ViewHolder con el binding
        return ExchangeRateViewHolder(binding)
    }

    // Este método vincula los datos con la vista del ViewHolder
    override fun onBindViewHolder(holder: ExchangeRateViewHolder, position: Int) {
        // Obtenemos el objeto ExchangeRateItem correspondiente al índice "position"
        val exchangeRate = exchangeRates[position]
        // Usamos el ViewHolder para vincular el objeto "exchangeRate" a la vista
        holder.bind(exchangeRate)
    }

    // Este método devuelve el número de elementos en la lista de tasas de cambio
    override fun getItemCount(): Int = exchangeRates.size

    // ViewHolder que mantiene el binding del layout del item
    class ExchangeRateViewHolder(private val binding: ItemExchangeRateBinding) : RecyclerView.ViewHolder(binding.root) {
        // Función para vincular los datos del objeto ExchangeRateItem a la vista
        fun bind(exchangeRate: ExchangeRateItem) {
            // Asignamos el código de la moneda (currencyCode) al TextView correspondiente
            binding.currencyCode.text = exchangeRate.currencyCode
            // Asignamos la tasa de cambio (rate) al TextView correspondiente
            binding.rate.text = exchangeRate.rate.toString()
        }
    }
}
