package com.smartguardian.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.smartguardian.databinding.FragmentHomeBinding
import com.smartguardian.data.RuleEntity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var ruleAdapter: RuleAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ruleAdapter = RuleAdapter(
            onToggle = { rule, isEnabled ->
                // TODO: ViewModel দিয়ে update করব
            },
            onEdit = { rule ->
                // TODO: AddRuleActivity তে navigate করব rule id দিয়ে
            }
        )

        binding.rvRules.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ruleAdapter
        }

        // TODO: ViewModel observe করে list update করব
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


class RuleAdapter(
    private val onToggle: (RuleEntity, Boolean) -> Unit,
    private val onEdit: (RuleEntity) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<RuleAdapter.RuleViewHolder>() {

    private var rules = listOf<RuleEntity>()

    fun submitList(newList: List<RuleEntity>) {
        rules = newList
        notifyDataSetChanged()
    }

    inner class RuleViewHolder(
        private val binding: com.smartguardian.databinding.ItemRuleBinding
    ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

        fun bind(rule: RuleEntity) {
            binding.tvRuleTitle.text = rule.title
            binding.tvRuleLabel.text = rule.label
            binding.tvRuleStatus.text = rule.status
            binding.switchRule.isChecked = rule.isEnabled

            binding.switchRule.setOnCheckedChangeListener { _, isChecked ->
                onToggle(rule, isChecked)
            }

            binding.btnEdit.setOnClickListener {
                onEdit(rule)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RuleViewHolder {
        val binding = com.smartguardian.databinding.ItemRuleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RuleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RuleViewHolder, position: Int) {
        holder.bind(rules[position])
    }

    override fun getItemCount() = rules.size
}