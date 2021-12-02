package site.yogaji.contactapp.customdialog;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.lang.ref.WeakReference;

        import android.content.Context;
        import android.os.AsyncTask;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.widget.Button;
        import android.widget.CheckBox;
        import android.widget.EditText;
        import android.widget.TextView;
        import android.widget.Toast;

        import androidx.appcompat.app.AlertDialog;
        import java.lang.ref.WeakReference;
        import site.yogaji.contactapp.DatabaseHelper;
        import site.yogaji.contactapp.IGenerateContactListener;
        import site.yogaji.contactapp.OperationTypeEnum;
        import site.yogaji.contactapp.R;
        import site.yogaji.contactapp.model.Contact;

public class ContactDialog  implements View.OnClickListener {
    private Context context;
    private AlertDialog alertDialog;
    private TextView titleTv;
    private EditText nameEt;
    private EditText telephoneEt;
    private EditText addressEt;
    private Button commitBtn;
    private Button cancelBtn;
    private IGenerateContactListener mListener;
    private Contact oldContact;
    private Context mContext;
    //    private CheckBox maleCb;
//    private CheckBox femaleCb;
    private OperationTypeEnum operationType;

    public ContactDialog(Context context,
                         OperationTypeEnum operationTypeEnum,
                         Contact contact,
                         IGenerateContactListener listener) {
        this.context = context;
        operationType = operationTypeEnum;
        oldContact = contact;
        mListener = listener;
        createDialogAndShow();
    }

    public ContactDialog(Context context,
                         OperationTypeEnum operationTypeEnum,
                         IGenerateContactListener listener) {
        this.context = context;
        operationType = operationTypeEnum;
        mListener = listener;
        createDialogAndShow();
    }

    public ContactDialog(Context context,
                         OperationTypeEnum operationTypeEnum,
                         Contact contact) {
        this.context = context;
        operationType = operationTypeEnum;
        oldContact = contact;
        createDialogAndShow();
    }

    public void createDialogAndShow() {
        View view = LayoutInflater.from(context).inflate(R.layout.add_contact, null);
        findView(view);
        initViewType();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        alertDialog = builder.create();
        if (operationType.equals(OperationTypeEnum.QUERY)) {
            alertDialog.setCanceledOnTouchOutside(true);
        } else {
            alertDialog.setCanceledOnTouchOutside(false);
        }
        alertDialog.show();

    }

    private void findView(View view) {
//        maleCb = view.findViewById(R.id.male_cb);
//        femaleCb = view.findViewById(R.id.female_cb);
//        View actionDialog = LayoutInflater.from(mContext).inflate(R.layout.action_dialog, null);
//        Button editBtn = actionDialog.findViewById(R.id.edit_btn);
//        Button deleteBtn = actionDialog.findViewById(R.id.delete_btn);
        titleTv = view.findViewById(R.id.dialog_title_tv);
        nameEt = view.findViewById(R.id.name_et);
        telephoneEt = view.findViewById(R.id.telephone_et);
        addressEt = view.findViewById(R.id.address_et);
        Button commitBtn = view.findViewById(R.id.commit_btn);
        Button cancelBtn = view.findViewById(R.id.cancel_btn);
        commitBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
//        maleCb.setOnClickListener(this);
//        femaleCb.setOnClickListener(this);
    }

    private void initViewType() {

        switch (operationType) {
            case INSERT:
                titleTv.setText("Add Contact");
                break;
            case UPDATE:
                titleTv.setText("Edit Contact!");
                initViewContent();
                break;
            case QUERY:
                titleTv.setText("Query Contact!");
                initViewContent();
                commitBtn.setVisibility(View.GONE);
                commitBtn.setEnabled(false);
                cancelBtn.setVisibility(View.GONE);
                cancelBtn.setEnabled(false);
//                maleCb.setEnabled(false);
//                femaleCb.setEnabled(false);
                nameEt.setEnabled(false);
                telephoneEt.setEnabled(false);
                addressEt.setEnabled(false);
                break;
            default:
                break;

        }
    }

    private void initViewContent() {
        if (oldContact != null) {
            nameEt.setText(oldContact.getName());
            telephoneEt.setText(oldContact.getTelephone());
            addressEt.setText(oldContact.getAddress());
//            if (oldBusinessCard.getGender()) {
//                maleCb.setChecked(true);
//            } else {
//                femaleCb.setChecked(true);
//            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.commit_btn:
                Toast.makeText(context, "OKKKK!", Toast.LENGTH_SHORT).show();
                Contact newContact = generateNewContact();
                if (newContact == null) {
                    Toast.makeText(context, "pls select gender!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (operationType.equals(OperationTypeEnum.UPDATE)) {
                    updateDatabaseContactInfo(newContact);
                } else if (operationType.equals(OperationTypeEnum.INSERT)) {
                    insertContactDataToDatabase(newContact);
                }

                alertDialog.dismiss();
                break;
            case R.id.cancel_btn:
                alertDialog.dismiss();
                break;
//            case R.id.male_cb:
//                //sure client only select one checkbox, can't select maleCb and female together
//                if (maleCb.isChecked()) {
//                    femaleCb.setChecked(false);
//                }
//                break;
//            case R.id.female_cb:
//                //sure client only select one checkbox, can't select maleCb and female together
//                if (femaleCb.isChecked()) {
//                    maleCb.setChecked(false);
//                }
//                break;
            default:
                break;
        }
    }

    private Contact generateNewContact() {
        Contact contact = new Contact();
//        if (!maleCb.isChecked() && !femaleCb.isChecked()) {
//            return null;
//        } else if (maleCb.isChecked()) {
//            businessCard.setGender(true);
//        } else {
//            businessCard.setGender(false);
//        }
        contact.setName(nameEt.getText().toString());
        contact.setTelephone(telephoneEt.getText().toString());
        contact.setAddress(addressEt.getText().toString());
//        contact.setAvatar(contact.getGender() ? R.drawable.male_avatar_icon : R.drawable.female_avatar_icon);

        return contact;
    }

    private void updateDatabaseContactInfo(Contact contact) {
        UpdateContactAsyncTask updateContactAsyncTask =
                new UpdateContactAsyncTask(context, oldContact.getId(), mListener);
        updateContactAsyncTask.execute(contact);
    }

    private void insertContactDataToDatabase(Contact contact) {
        InsertContactAsyncTask insertContactAsyncTask = new InsertContactAsyncTask(context, mListener);
        insertContactAsyncTask.execute(contact);
    }


    private static class UpdateContactAsyncTask extends AsyncTask<Contact, Void, Contact> {

        private WeakReference<Context> contextWeakReference;
        private IGenerateContactListener iGenerateContactListener;
        private int oldContactId;

        UpdateContactAsyncTask(Context context, int id, IGenerateContactListener listener) {
            contextWeakReference = new WeakReference<>(context);
            oldContactId = id;
            iGenerateContactListener = listener;
        }

        @Override
        protected Contact doInBackground(Contact... contacts) {
            Context context = contextWeakReference.get();
            if (context != null) {
                DatabaseHelper databaseHelper = new DatabaseHelper(context);
                int theNumberAfterUpdate = databaseHelper.updateContact(oldContactId, contacts[0]);
                if (theNumberAfterUpdate > 0) {
                    return contacts[0];
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Contact contact) {
            super.onPostExecute(contact);
            Context context = contextWeakReference.get();
            if (context != null) {
                if (contact != null) {
                    iGenerateContactListener.getContact(contact);
                } else {
                    Toast.makeText(context, "Insert BusinessCard Failed!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private static class InsertContactAsyncTask extends AsyncTask<Contact, Void, Contact> {
        private WeakReference<Context> contextWeakReference;
        private IGenerateContactListener iGenerateContactListener;

        InsertContactAsyncTask(Context context, IGenerateContactListener listener) {
            contextWeakReference = new WeakReference<>(context);
            iGenerateContactListener = listener;
        }

        @Override
        protected Contact doInBackground(Contact... contacts) {
            Contact contact = contacts[0];
            long idReturnByInsert = -1;
            if (contextWeakReference.get() != null) {
                idReturnByInsert = new DatabaseHelper(contextWeakReference.get()).insertContact(contact);
            }
            if (idReturnByInsert > -1) {
                contact.setId((int) idReturnByInsert);
                return contact;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Contact contact) {
            super.onPostExecute(contact);
            if (contextWeakReference.get() != null) {
                if (contact != null) {
                    iGenerateContactListener.getContact(contact);
                } else {
                    Toast.makeText(contextWeakReference.get(), "Insert BusinessCard Failed!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}