package com.example.expenseapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.example.expenseapp.databinding.FragmentExpenseDetailBinding
import kotlinx.coroutines.launch
import java.util.Date


class ExpenseDetailFragment: Fragment() {
    private var _binding: FragmentExpenseDetailBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    // the expenseId is passed to ExpenseDetailFragment by navigation. Now we need to pass it to ViewModel
    private val args: ExpenseDetailFragmentArgs by navArgs()
    // load the ViewModel via ViewModelFactory instead. Because it's gonna accept an argument called 'expenseId', which is unusual
    private val expenseDetailViewModel: ExpenseDetailViewModel by viewModels {
        ExpenseDetailViewModelFactory(args.expenseId)
    }

    private lateinit var expense: Expense

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // inflate the _binding when the fragment is being created
        _binding = FragmentExpenseDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUi()

        // When the expenseDetailFragment is create, bind a getter() to the ViewModel.
        // Whenever the ViewModel is started, the getter() collect expense from the flow and set the UI
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // the following will runs every time ViewModel is started
                expenseDetailViewModel.expense.collect { expense: Expense? ->
                    // Note: expense will be null when this ExpenseDetailedFragment is launched by menu_add_button
                    expense?.let { updateUi(it) }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    /**
     * Set the UI listeners / UI layout. Do not set UI data here. (data will be set by updateUi())
     */
    private fun setUi() {
        binding.apply {
            // bind some listeners to Button, TextView, ...
            expenseTitleEditText.doOnTextChanged { text, start, before, count ->
                expenseDetailViewModel.updateExpenseData { oldExpense: Expense ->
                    val textString: String = text.toString()
                    oldExpense.copy(title = textString)
                }
            }


            expenseCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val categoryStringList: List<String> = enumValues<Category>().map { it.name }
                    val selectedCategory: Category = enumValueOf<Category>(categoryStringList[position])
                    expenseDetailViewModel.updateExpenseData { oldExpense: Expense ->
                        oldExpense.copy(category = selectedCategory)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    expenseDetailViewModel.updateExpenseData { oldExpense: Expense ->
                        oldExpense.copy(category = Category.Food)
                    }
                }

            }

            expenseDateButton.apply {
                isEnabled = false
            }

            expenseAmountEditText.doOnTextChanged { text, start, before, count ->
                expenseDetailViewModel.updateExpenseData { oldExpense: Expense ->
                    val textString = text.toString()
                    val textFloat = if(!textString.isEmpty()) textString.toFloat() else 0.0f
                    oldExpense.copy(amount = textFloat)
                }
            }

            expenseDescriptionEditText.doOnTextChanged { text, start, before, count ->
                expenseDetailViewModel.updateExpenseData { oldExpense: Expense ->
                    val textString = text.toString()
                    oldExpense.copy(description = textString)
                }

            }
        }
    }

    private fun updateUi(expense: Expense) {
        binding.apply {
            expenseCategorySpinner.setSelection(expense.category.ordinal)
            expenseTitleEditText.setText(expense.title)
            expenseDateButton.text = expense.date.toString()
            expenseAmountEditText.setText(expense.amount.toString())
            expenseDescriptionEditText.setText(expense.description ?: "No description provided")
        }
    }
}