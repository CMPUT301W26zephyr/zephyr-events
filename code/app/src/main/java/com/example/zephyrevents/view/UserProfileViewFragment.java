package com.example.zephyrevents.view;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.UserController;
import com.example.zephyrevents.repository.RepositoryCallback;

import android.text.TextUtils;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import com.bumptech.glide.Glide;



/**
 * Activity that displays the user profile.
 * Allows navigation to viewing notifications, settings, and editing profile details.
 * Allows deleting user account.
 */
public class UserProfileViewFragment extends Fragment {
    private UserController userController;

    private TextView txtName;
    private TextView txtContact;
    private ImageView avatarImg;

    private ActivityResultLauncher<PickVisualMediaRequest> pickProfileImage;
    private boolean profileHasAvatar = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_user, container, false); // Rename your layout files if you want
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userController = new UserController(requireContext());

        txtName = view.findViewById(R.id.txtName);
        txtContact = view.findViewById(R.id.txtContact);
        avatarImg = view.findViewById(R.id.avatar_img);
        pickProfileImage = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri == null) return;
                    userController.updateProfileImg(uri, new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(requireContext(), "Photo updated", Toast.LENGTH_SHORT).show();
                            refreshProfile();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(requireContext(),
                                    "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
        );



        setUpClickListener(view);
        refreshProfile();

    }

    @Override
    public void onResume(){
        super.onResume();
        refreshProfile();
    }

    private void refreshProfile(){
        userController.getCurrentUserProfileInfo(new RepositoryCallback<String[]>() {
            @Override
            public void onSuccess(String[] data) {
                String name = (data != null && data.length > 0 && data[0] != null) ? data[0] : "";
                String email = (data != null && data.length > 1 && data[1] != null) ? data[1] : "";
                String phone = (data != null && data.length > 2 && data[2] != null) ? data[2] : "";

                String contactinfo;
                if (!email.isEmpty() && !phone.isEmpty()) contactinfo = email + " | " + phone;
                else if (!email.isEmpty()) contactinfo = email;
                else if (!phone.isEmpty()) contactinfo = phone;
                else contactinfo = "";

                txtName.setText(name.isEmpty() ? "John Doe" : name);
                txtContact.setText(contactinfo);

                String avatarUrl = (data != null && data.length > 4 && data[4] != null) ? data[4] : "";
                boolean hasAvatar = !TextUtils.isEmpty(avatarUrl);
                profileHasAvatar = hasAvatar;

                if (hasAvatar){
                    Glide.with(requireContext())
                            .load(avatarUrl)
                            .circleCrop()
                            .into(avatarImg);
                } else{
                    Glide.with(requireContext()).clear(avatarImg);
                    avatarImg.setImageResource(R.drawable.ic_person_24);
                }



            }

            @Override
            public void onFailure(Exception e) {
                if (!userController.isUserLoggedIn()){
                    Toast.makeText(requireContext(), "Session expired or account removed.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(requireContext(), WelcomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();

                } else{
                    Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();

                }

            }
        });
    }

    private void setUpClickListener(View view){
        view.findViewById(R.id.btnEditAvatar).setOnClickListener(v -> {
            if(profileHasAvatar) {
                showAvatarOptionDialog();
            } else{
                launchOptionProfileAvatar();
            }
        });

        view.findViewById(R.id.rowEditProfile).setOnClickListener(v -> openEditProfile());

        // Split the Notification Actions
        view.findViewById(R.id.rowNotifications).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), UserNotificationListView.class));
        });
        view.findViewById(R.id.rowNotificationSettings).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), UserProfileSettingsViewActivity.class));
        });

        view.findViewById(R.id.rowTC).setOnClickListener(v -> { /* TODO: Open Terms */ });
        view.findViewById(R.id.rowDeleteProfile).setOnClickListener(v -> showDeleteConfirmDialog());

    }

    private void launchOptionProfileAvatar(){
        pickProfileImage.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void showConfirmRemoveAvatarDialog(){
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_confirm_remove_avatar, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialogView.findViewById(R.id.btnDialogCancel).setOnClickListener(v -> {
            dialog.dismiss();
        });
        dialogView.findViewById(R.id.btnDialogConfirm).setOnClickListener(v -> {
            dialog.dismiss();
            userController.clearProfileAvatar(new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Toast.makeText(requireContext(), "Avatar removed", Toast.LENGTH_SHORT).show();
                    refreshProfile();

                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(requireContext(),
                            "Could not remove profile: " + e.getMessage(), Toast.LENGTH_LONG).show();


                }
            });
        });


        dialog.show();
    }

    private void showAvatarOptionDialog(){
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_avatar, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialogView.findViewById(R.id.btnDialogCancel).setOnClickListener(v -> {
            dialog.dismiss();
            launchOptionProfileAvatar();
        });

        dialogView.findViewById(R.id.btnDialogConfirm).setOnClickListener(v -> {
            dialog.dismiss();
            showConfirmRemoveAvatarDialog();
        });
        dialog.show();
    }




    private void openEditProfile(){
        startActivity(new Intent(requireContext(), UserProfileEditViewActivity.class));
    }

    private void showDeleteConfirmDialog(){
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_profile_confirm, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();
        dialogView.findViewById(R.id.btnDialogCancel).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btnDialogConfirm).setOnClickListener(v -> {
            dialog.dismiss();

            // Use the controller to handle the logic
            userController.deleteAccount(new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
//                    Toast.makeText(UserProfileViewActivity.this, "Account Deleted", Toast.LENGTH_SHORT).show();

                    // Navigate back to WelcomeActivity and clear the stack
                    Intent intent = new Intent(requireContext(), WelcomeActivity.class);
                    intent.putExtra("TOAST_MESSAGE", "Account Deleted!");  // pass to notify the user of what happened

                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(requireContext(), "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
        dialog.show();
    }
}