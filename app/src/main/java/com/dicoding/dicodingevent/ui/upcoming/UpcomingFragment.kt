@file:Suppress("unused", "RedundantSuppression")

package com.dicoding.dicodingevent.ui.upcoming

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.dicodingevent.data.local.entity.EventEntity
import com.dicoding.dicodingevent.data.repo.Result
import com.dicoding.dicodingevent.databinding.FragmentUpcomingBinding
import com.dicoding.dicodingevent.ui.ViewModelFactory
import com.dicoding.dicodingevent.ui.adapter.EventAdapter
import com.dicoding.dicodingevent.ui.detail.DetailActivity

class UpcomingFragment : Fragment() {

    private var _binding: FragmentUpcomingBinding? = null
    private val binding get() = _binding!!
    private lateinit var eventAdapter: EventAdapter
    private val upcomingViewModel: UpcomingViewModel by viewModels { ViewModelFactory.getInstance(requireActivity()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpcomingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchView()
        observeUpcomingEvents()
    }

    private fun setupRecyclerView() {
        eventAdapter = EventAdapter { navigateToDetailEvent(it) }
        binding.rvEvent.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = eventAdapter
        }
    }

    private fun setupSearchView() {
        binding.searchView.apply {
            visibility = View.VISIBLE
            setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    observeSearchUpcomingEvents(query.orEmpty())
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    observeSearchUpcomingEvents(newText.orEmpty())
                    return true
                }
            })
        }
    }

    private fun observeUpcomingEvents() {
        upcomingViewModel.findUpcomingEvents().observe(viewLifecycleOwner) { result ->
            handleResult(result)
        }
    }

    private fun observeSearchUpcomingEvents(query: String) {
        upcomingViewModel.searchUpcomingEvents(query).observe(viewLifecycleOwner) { result ->
            handleResult(result)
        }
    }

    private fun handleResult(result: Result<List<EventEntity>>) {
        when (result) {
            is Result.Loading -> showLoading(true)
            is Result.Success -> {
                showLoading(false)
                updateEventList(result.data)
            }
            is Result.Error -> showError()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.tvNoEvent.visibility = if (isLoading) View.GONE else binding.tvNoEvent.visibility
    }

    private fun updateEventList(eventData: List<EventEntity>) {
        if (eventData.isEmpty()) {
            binding.tvNoEvent.visibility = View.VISIBLE
            binding.rvEvent.visibility = View.GONE
        } else {
            binding.tvNoEvent.visibility = View.GONE
            binding.rvEvent.visibility = View.VISIBLE
            eventAdapter.submitList(eventData)
        }
    }

    private fun showError() {
        binding.progressBar.visibility = View.GONE
        binding.rvEvent.visibility = View.GONE
        binding.tvNoEvent.visibility = View.VISIBLE
    }

    private fun navigateToDetailEvent(event: EventEntity) {
        val intent = Intent(requireContext(), DetailActivity::class.java).apply {
            putExtra(DetailActivity.EXTRA_EVENT_ID, event.id.toString())
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
