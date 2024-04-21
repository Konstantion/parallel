with Ada.Text_IO;                                   use Ada.Text_IO;
with GNAT.Semaphores;                               use GNAT.Semaphores;
with Ada.Containers.Indefinite_Doubly_Linked_Lists; use Ada.Containers;

procedure Main is
    package Integer_Lists is new Indefinite_Doubly_Linked_Lists (Integer);
    use Integer_Lists;
    Items_Storage : List;

    Space_Available : Counting_Semaphore (10, Default_Ceiling);
    Items_Available : Counting_Semaphore (0, Default_Ceiling);

    task type Producer is
        entry Start (Items_To_Produce, ID : Integer);
    end Producer;

    task type Consumer is
        entry Start (Items_To_Consume, ID : Integer);
    end Consumer;

    task body Producer is
        Items_To_Produce : Integer;
        ID               : Integer;
    begin
        accept Start (Items_To_Produce, ID : Integer) do
            Producer.ID               := ID;
            Producer.Items_To_Produce := Items_To_Produce;
        end Start;

        for I in 1 .. Producer.Items_To_Produce loop
            Space_Available.Seize;
            Items_Storage.Append (I);
            Put_Line
               ("Producer " & Integer'Image (Producer.ID) & " added item: " &
                Integer'Image (I));
            Items_Available.Release;
        end loop;
    end Producer;

    task body Consumer is
        Items_To_Consume : Integer;
        ID               : Integer;
    begin
        accept Start (Items_To_Consume, ID : Integer) do
            Consumer.Items_To_Consume := Items_To_Consume;
            Consumer.ID               := ID;
        end Start;

        for I in 1 .. Items_To_Consume loop
            Items_Available.Seize;

            declare
                Item : Integer := First_Element (Items_Storage);
            begin
                Put_Line
                   ("Consumer " & Integer'Image (Consumer.ID) &
                    " removed item: " & Integer'Image (Item));
                Items_Storage.Delete_First;
            end;

            Space_Available.Release;
        end loop;
    end Consumer;

    type Producer_Array is array (Positive range <>) of Producer;
    type Consumer_Array is array (Positive range <>) of Consumer;

    Producers : Producer_Array (1 .. 5);
    Consumers : Consumer_Array (1 .. 3);

    procedure Initiate_Production_and_Consumption is
    begin
        for I in Producers'Range loop
            Producers (I).Start (10, I);
        end loop;

        for J in Consumers'Range loop
            Consumers (J).Start (10, J);
        end loop;
    end Initiate_Production_and_Consumption;

begin
    Initiate_Production_and_Consumption;
end Main;

