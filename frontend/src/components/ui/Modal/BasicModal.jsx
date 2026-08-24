import Box from '@mui/material/Box';
import Modal from '@mui/material/Modal';
import IconButton from '@mui/material/IconButton'
import CloseIcon from '@mui/icons-material/Close'

const style = {
    position: 'relative',
    top: '50%',
    left: '50%',
    transform: 'translate(-50%, -50%)',
    width: 400,
    bgcolor: 'background.paper',
    borderRadius: '10px',
    boxShadow: 24,
    p: 4,
};

export default function BasicModal({open, handleClose, children}) {
    return (
        <div>
            <Modal
                open={open}
                onClose={handleClose}
            >
                <Box sx={style}>
                    <IconButton 
                        onClick={handleClose}
                        sx={{ position: "absolute", top: 8, right: 8 }}
                        >
                        <CloseIcon sx={{ "&:hover": { color: "red" } }}/>
                    </IconButton>
                    {children}
                </Box>
            </Modal>
        </div>
    );
}