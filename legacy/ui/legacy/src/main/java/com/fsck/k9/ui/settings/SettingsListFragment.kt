package com.fsck.k9.ui.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.k9mail.core.ui.legacy.designsystem.atom.icon.Icons
import app.k9mail.feature.launcher.FeatureLauncherActivity
import com.fsck.k9.Account
import com.fsck.k9.ui.R
import com.fsck.k9.ui.base.livedata.observeNotNull
import com.fsck.k9.ui.settings.account.AccountSettingsActivity
import com.fsck.k9.view.DraggableFrameLayout
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.GenericItem
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.drag.ItemTouchCallback
import com.mikepenz.fastadapter.drag.SimpleDragCallback
import com.mikepenz.fastadapter.utils.DragDropUtil
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsListFragment : Fragment(), ItemTouchCallback {
    private val viewModel: SettingsViewModel by viewModel()

    private lateinit var itemAdapter: ItemAdapter<GenericItem>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initializeSettingsList(recyclerView = view.findViewById(R.id.settings_list))
        populateSettingsList()
    }

    private fun initializeSettingsList(recyclerView: RecyclerView) {
        itemAdapter = ItemAdapter()

        val touchCallBack = SimpleDragCallback(this).apply {
            setIsDragEnabled(true)
        }
        val touchHelper = ItemTouchHelper(touchCallBack)

        val settingsListAdapter = FastAdapter.with(itemAdapter).apply {
            setHasStableIds(true)
            onClickListener = { _, _, item, _ ->
                handleItemClick(item)
                true
            }
            addEventHook(
                DragHandleTouchEvent { position ->
                    recyclerView.findViewHolderForAdapterPosition(position)?.let { viewHolder ->
                        touchHelper.startDrag(viewHolder)
                    }
                },
            )
        }

        recyclerView.adapter = settingsListAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        touchHelper.attachToRecyclerView(recyclerView)
    }

    private fun populateSettingsList() {
        viewModel.accounts.observeNotNull(this) { accounts ->
            if (accounts.isEmpty()) {
                launchOnboarding()
            } else {
                populateSettingsList(accounts)
            }
        }
    }

    private fun populateSettingsList(accounts: List<Account>) {
        val listItems = buildSettingsList {
            addAction(
                text = getString(R.string.general_settings_title),
                navigationAction = R.id.action_settingsListScreen_to_generalSettingsScreen,
                icon = Icons.Outlined.Settings,
            )

            addSection(title = getString(R.string.accounts_title)) {
                val isDraggable = accounts.size > 1
                for (account in accounts) {
                    addAccount(account, isDraggable)
                }
/* not used in DDD mail
                addAction(
                    text = getString(R.string.add_account_action),
                    navigationAction = R.id.action_settingsListScreen_to_addAccountScreen,
                    icon = Icons.Outlined.Add,
                )
 */
            }

            /* not used in DDD mail
            addSection(title = getString(R.string.settings_list_backup_category)) {
                addAction(
                    text = getString(R.string.settings_export_title),
                    navigationAction = R.id.action_settingsListScreen_to_settingsExportScreen,
                    icon = Icons.Outlined.Upload,
                )

                addAction(
                    text = getString(SettingsImportR.string.settings_import_title),
                    navigationAction = R.id.action_settingsListScreen_to_settingsImportScreen,
                    icon = Icons.Outlined.Download,
                )
            }
             */

            addSection(title = getString(R.string.settings_list_miscellaneous_category)) {
                addAction(
                    text = getString(R.string.about_action),
                    navigationAction = R.id.action_settingsListScreen_to_aboutScreen,
                    icon = Icons.Outlined.Info,
                )

                addUrlAction(
                    text = getString(R.string.user_manual_title),
                    url = getString(R.string.user_manual_url),
                    icon = Icons.Outlined.MenuBook,
                )

                addUrlAction(
                    text = getString(R.string.get_help_title),
                    url = getString(R.string.user_forum_url),
                    icon = Icons.Outlined.Help,
                )
            }
        }

        itemAdapter.setNewList(listItems)
    }

    private fun handleItemClick(item: GenericItem) {
        when (item) {
            is AccountItem -> launchAccountSettings(item.account)
            is UrlActionItem -> openUrl(item.url)
            is SettingsActionItem -> findNavController().navigate(item.navigationAction)
        }
    }

    private fun openUrl(url: String) {
        try {
            val viewIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(viewIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), R.string.error_activity_not_found, Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchAccountSettings(account: Account) {
        AccountSettingsActivity.start(requireActivity(), account.uuid)
    }

    private fun launchOnboarding() {
        FeatureLauncherActivity.launchOnboarding(requireActivity())

        requireActivity().finishAffinity()
    }

    private fun buildSettingsList(block: SettingsListBuilder.() -> Unit): List<GenericItem> {
        return SettingsListBuilder().apply(block).toList()
    }

    internal class SettingsListBuilder {
        private val settingsList = mutableListOf<GenericItem>()
        private var itemId = 0L

        fun addAction(text: String, @IdRes navigationAction: Int, @DrawableRes icon: Int) {
            itemId++
            settingsList.add(SettingsActionItem(itemId, text, navigationAction, icon))
        }

        fun addUrlAction(text: String, url: String, @DrawableRes icon: Int) {
            itemId++
            settingsList.add(UrlActionItem(itemId, text, url, icon))
        }

        fun addAccount(account: Account, isDraggable: Boolean) {
            settingsList.add(AccountItem(account, isDraggable))
        }

        fun addSection(title: String, block: SettingsListBuilder.() -> Unit) {
            itemId++
            settingsList.add(SettingsDividerItem(itemId, title))
            block()
        }

        fun toList(): List<GenericItem> = settingsList
    }

    override fun itemTouchStartDrag(viewHolder: RecyclerView.ViewHolder) {
        (viewHolder.itemView as DraggableFrameLayout).isDragged = true
    }

    override fun itemTouchStopDrag(viewHolder: RecyclerView.ViewHolder) {
        (viewHolder.itemView as DraggableFrameLayout).isDragged = false
    }

    override fun itemTouchOnMove(oldPosition: Int, newPosition: Int): Boolean {
        val firstDropPosition = itemAdapter.adapterItems.indexOfFirst { it is AccountItem }
        val lastDropPosition = itemAdapter.adapterItems.indexOfLast { it is AccountItem }

        return if (newPosition in firstDropPosition..lastDropPosition) {
            DragDropUtil.onMove(itemAdapter, oldPosition, newPosition)
            true
        } else {
            false
        }
    }

    override fun itemTouchDropped(oldPosition: Int, newPosition: Int) {
        if (oldPosition == newPosition) return

        val account = (itemAdapter.getAdapterItem(newPosition) as AccountItem).account
        val firstAccountPosition = itemAdapter.adapterItems.indexOfFirst { it is AccountItem }
        val newAccountPosition = newPosition - firstAccountPosition

        viewModel.moveAccount(account, newAccountPosition)
    }
}
