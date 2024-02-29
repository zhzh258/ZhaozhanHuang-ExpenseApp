package com.example.expenseapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expenseapp.databinding.FragmentExpenseListBinding
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID


class ExpenseListFragment: Fragment() {
    private var _binding: FragmentExpenseListBinding? = null
    private val binding: FragmentExpenseListBinding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }
    private val expenseListViewModel: ExpenseListViewModel by viewModels()

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentExpenseListBinding.inflate(inflater, container, false)
        binding.expenseRecyclerView.layoutManager = LinearLayoutManager(context)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // When the expenseListFragment is create, bind a getter() to the ViewModel.
        // Whenever the ViewModel is started, the getter() collect expense from the flow and set the adapter
        viewLifecycleOwner.lifecycleScope.launch() {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // the following will runs every time ViewModel is started
                Log.d("ExpenseListFragmentDebug", "the following will runs every time ExpenseListViewModel is started")
                setUpCategorySpinnerAdapter()
                setUpTimeRangeSpinnerAdapter()
                expenseListViewModel.expenses.collect {
//                    Log.d("MyDebug", "expenseListViewModel.expenses.collect, size: ${it.size}")
                    setUpRecyclerViewAdapter(it, this@ExpenseListFragment::handleItemClicked)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @Suppress("DEPRECATION")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(com.example.expenseapp.R.menu.fragment_expense_list, menu)
        setHasOptionsMenu(true)
    }

    @Suppress("DEPRECATION")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // currently I have only 1 item, which is new_expense (The '+' icon)
            R.id.new_expense -> {
                handleAddButtonClicked()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /*
        The following are util functions
            1. handleItemClicked(expenseId: UUID)
            2. setUpRecyclerViewAdapter(expenses: List<Expense>, handleItemClicked: (UUID) -> Unit)
            3. setUpSpinnerAdapter()
     */

    private fun handleItemClicked(expenseId: UUID): Unit {
        // args.expenseId == UUID: open and a specific Expense
        findNavController().navigate(ExpenseListFragmentDirections.actionExpenseListFragmentToExpenseDetailFragment(expenseId))
    }

    private fun handleAddButtonClicked(): Unit {
        // args.expenseId == null: open an empty new Expense
        findNavController().navigate(ExpenseListFragmentDirections.actionExpenseListFragmentToExpenseDetailFragment(null))
    }

    private fun setUpRecyclerViewAdapter(expenses: List<Expense>, handleItemClicked: (UUID) -> Unit) {
//        Log.d("MyDebug", "setUpRecyclerViewAdapter, expenses has ${expenses.size} elements")
        binding.expenseRecyclerView.adapter = ExpenseListAdapter(expenses, handleItemClicked)
    }

    private fun setUpCategorySpinnerAdapter() {
        Log.d("ExpenseListFragmentDebug", "setting up category spinner adapter...")
        val optionStringList: List<String> = listOf("All Categories") + enumValues<Category>().map { it.name }

        // Note: Here simple_spinner_item comes from android.R. Not from com.example.expenseapp.R!
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, optionStringList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.expenseCategorySpinner.adapter = adapter

        binding.expenseCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedOptionString = optionStringList[position]
                val selectedCategory: Category? = when(selectedOptionString){
                    "All Categories" -> null
                    else -> enumValueOf<Category>(selectedOptionString)
                }
                expenseListViewModel.selection1 = selectedCategory

                var startDate: Date?
                when(expenseListViewModel.selection2) {
                    TimeRange.OneDay -> {
                        startDate = Date()
                        startDate.time -= 24 * 60 * 60 * 1000 * 1
                    }
                    TimeRange.ThreeDay -> {
                        startDate = Date()
                        startDate.time -= 24 * 60 * 60 * 1000 * 3
                    }
                    TimeRange.OneWeek -> {
                        startDate = Date()
                        startDate.time -= 24 * 60 * 60 * 1000 * 7
                    }
                    null -> {
                        startDate = null
                    }
                }
                Log.d("SpinnerDebug", "expenseListViewModel.getExpensesData(${expenseListViewModel.selection1}, ${startDate})")
                expenseListViewModel.getExpensesData(expenseListViewModel.selection1, startDate)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                expenseListViewModel.getExpensesData()
            }
        }
    }

    private fun setUpTimeRangeSpinnerAdapter() {
        Log.d("ExpenseListFragmentDebug", "setting up date spinner adapter...")

        val optionStringList: List<String> = listOf("Any Time") + enumValues<TimeRange>().map { it.name }
        // Note: Here simple_spinner_item comes from android.R. Not from com.example.expenseapp.R!
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, optionStringList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.expenseTimeRangeSpinner.adapter = adapter

        binding.expenseTimeRangeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedOptionString = optionStringList[position]
                val selectedTimeRange: TimeRange? = when(selectedOptionString){
                    "Any Time" -> null
                    else -> enumValueOf<TimeRange>(selectedOptionString)
                }
                expenseListViewModel.selection2 = selectedTimeRange

                var startDate: Date?
                when(expenseListViewModel.selection2) {
                    TimeRange.OneDay -> {
                        startDate = Date()
                        startDate.time -= 24 * 60 * 60 * 1000 * 1
                    }
                    TimeRange.ThreeDay -> {
                        startDate = Date()
                        startDate.time -= 24 * 60 * 60 * 1000 * 3
                    }
                    TimeRange.OneWeek -> {
                        startDate = Date()
                        startDate.time -= 24 * 60 * 60 * 1000 * 7
                    }
                    null -> {
                        startDate = null
                    }
                }
                Log.d("SpinnerDebug", "expenseListViewModel.getExpensesData(${expenseListViewModel.selection1}, ${startDate})")
                expenseListViewModel.getExpensesData(expenseListViewModel.selection1, startDate)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                expenseListViewModel.getExpensesData()
            }
        }
    }
}
