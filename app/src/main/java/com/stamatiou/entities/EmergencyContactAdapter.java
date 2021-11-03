// -------------------------------------------------------------
//
// This is the EmergencyContact Adapter used by the application, to
// populate the corresponding Recycler View in EmergencyContacts Activity.
// Each generated record can be long-clicked, to edit or delete it.
//
// Author: Aggelos Stamatiou, September 2020
//
// --------------------------------------------------------------

package com.stamatiou.entities;

import android.content.Context;
import android.content.Intent;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.stamatiou.smartalert.EmergencyContactsActionActivity;
import com.stamatiou.smartalert.R;

import java.util.ArrayList;
import java.util.List;

public class EmergencyContactAdapter extends RecyclerView.Adapter<EmergencyContactAdapter.EmergencyContactViewHolder> {

    private List<EmergencyContact> emergencyContacts;

    public static class EmergencyContactViewHolder extends RecyclerView.ViewHolder {

        private List<EmergencyContact> emergencyContacts;
        private TextView nameView, surnameView, phoneView;

        public EmergencyContactViewHolder(final View itemView, List<EmergencyContact> emergencyContactsList) {
            super(itemView);
            this.emergencyContacts = emergencyContactsList;
            this.nameView = itemView.findViewById(R.id.nameView);
            this.surnameView = itemView.findViewById(R.id.surnameView);
            this.phoneView = itemView.findViewById(R.id.phoneView);
            itemView.setOnLongClickListener( new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Context wrapper = new ContextThemeWrapper(v.getContext(), R.style.PopupMenuStyle);
                    PopupMenu popupMenu = new PopupMenu(wrapper, v);
                    popupMenu.setGravity(Gravity.RIGHT);
                    popupMenu.getMenu().add(0, 1, 0, R.string.edit).setActionView(v);
                    popupMenu.getMenu().add(0, 2, 1, R.string.delete).setActionView(v);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            Intent intent = new Intent(itemView.getContext(), EmergencyContactsActionActivity.class);
                            intent.putExtra("mode", item.getItemId());
                            intent.putExtra("emergencyContacts", new ArrayList<>(emergencyContacts));
                            intent.putExtra("name", nameView.getText().toString().replace("Name: ", ""));
                            intent.putExtra("surname", surnameView.getText().toString().replace("Surname: ", ""));
                            intent.putExtra("phone", phoneView.getText().toString().replace("Phone: ", ""));
                            itemView.getContext().startActivity(intent);
                            return true;
                        }
                    });
                    popupMenu.show();
                    return true;
                }
            } );
        }
    }

    public EmergencyContactAdapter(List<EmergencyContact> emergencyContacts) {
        this.emergencyContacts = emergencyContacts;
    }

    @Override
    public EmergencyContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.emergency_contact_card_layout, parent, false);
        EmergencyContactViewHolder emergencyContactViewHolder = new EmergencyContactViewHolder(view, emergencyContacts);
        return emergencyContactViewHolder;
    }

    @Override
    public void onBindViewHolder(EmergencyContactViewHolder holder, int position) {
        TextView nameView = holder.nameView;
        TextView surnameView = holder.surnameView;
        TextView phoneView = holder.phoneView;
        nameView.setText(nameView.getResources().getString(R.string.name) + emergencyContacts.get(position).getName());
        surnameView.setText(surnameView.getResources().getString(R.string.surname) + emergencyContacts.get(position).getSurname());
        phoneView.setText(phoneView.getResources().getString(R.string.phone) + emergencyContacts.get(position).getPhone());
    }

    @Override
    public int getItemCount() {
        return emergencyContacts.size();
    }

}
